param(
    [string]$SnapshotFile = "eduscope-frontend\\public\\demo-snapshot\\snapshot.json"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot

if ([System.IO.Path]::IsPathRooted($SnapshotFile)) {
    $fullPath = $SnapshotFile
}
else {
    $fullPath = Join-Path $repoRoot $SnapshotFile
}

if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
    throw ("Snapshot file not found: {0}" -f $fullPath)
}

$file = Get-Item -LiteralPath $fullPath

Write-Host ""
Write-Host "EduScope Snapshot Validation"
Write-Host ("File: {0}" -f $fullPath)
Write-Host ("Size: {0:N2} MB" -f ($file.Length / 1MB))
Write-Host ""

$rawJson = [System.IO.File]::ReadAllText(
    $fullPath,
    [System.Text.Encoding]::UTF8
)

try {
    $snapshot = ConvertFrom-Json -InputObject $rawJson
}
catch {
    throw ("Invalid snapshot JSON: {0}" -f $_.Exception.Message)
}

if ($null -eq $snapshot.responses) {
    throw "Snapshot responses object is missing."
}

if ($null -eq $snapshot.studentSearchIndex) {
    throw "Snapshot studentSearchIndex is missing."
}

$responseProperties = @(
    $snapshot.responses.PSObject.Properties
)

$courseProperty = @(
    $responseProperties |
    Where-Object {
        $_.Name -eq "/api/courses"
    }
) | Select-Object -First 1

if ($null -eq $courseProperty) {
    throw "Snapshot /api/courses response is missing."
}

$courseCount = @(
    $courseProperty.Value
).Count

$studentIndexCount = @(
    $snapshot.studentSearchIndex
).Count

$storageMode = $snapshot.metadata.storageMode
$studentDetailCount = 0
$shardFiles = @()

if ($storageMode -eq "SHARDED_V1") {
    $snapshotDirectory = [System.IO.Path]::GetDirectoryName($fullPath)
    $studentDirectory = Join-Path $snapshotDirectory "students"

    if (-not (Test-Path -LiteralPath $studentDirectory -PathType Container)) {
        throw "Snapshot students directory is missing."
    }

    $shardFiles = @(
        Get-ChildItem -LiteralPath $studentDirectory -Filter "page-*.json" -File |
        Sort-Object Name
    )

    if ($shardFiles.Count -eq 0) {
        throw "Snapshot student shard files are missing."
    }

    foreach ($shardFile in $shardFiles) {
        $shardRawJson = [System.IO.File]::ReadAllText(
            $shardFile.FullName,
            [System.Text.Encoding]::UTF8
        )

        try {
            $shard = ConvertFrom-Json -InputObject $shardRawJson
        }
        catch {
            throw ("Invalid shard JSON: {0} / {1}" -f $shardFile.Name, $_.Exception.Message)
        }

        if ($null -eq $shard.responses) {
            throw ("Shard responses object is missing: {0}" -f $shardFile.Name)
        }

        $studentDetailCount += @(
            $shard.responses.PSObject.Properties |
            Where-Object {
                $_.Name -match '^/api/student-analysis/[0-9]+$'
            }
        ).Count
    }
}
else {
    $studentDetailCount = @(
        $responseProperties |
        Where-Object {
            $_.Name -match '^/api/student-analysis/[0-9]+$'
        }
    ).Count
}

$displayStorageMode = if ($null -eq $storageMode) {
    "MONOLITHIC"
}
else {
    $storageMode
}

Write-Host ("Storage mode: {0}" -f $displayStorageMode)
Write-Host ("Responses: {0}" -f $responseProperties.Count)
Write-Host ("Courses: {0}" -f $courseCount)
Write-Host ("Student details: {0}" -f $studentDetailCount)
Write-Host ("Student index rows: {0}" -f $studentIndexCount)

if ($storageMode -eq "SHARDED_V1") {
    Write-Host ("Student shard files: {0}" -f $shardFiles.Count)
}

if ($studentDetailCount -ne $studentIndexCount) {
    throw ("Student detail/index count mismatch. details={0}, index={1}" -f $studentDetailCount, $studentIndexCount)
}

$forbiddenChecks = @(
    [PSCustomObject]@{
        Name = "passwordHash field"
        Pattern = '"passwordHash"\s*:'
    },
    [PSCustomObject]@{
        Name = "ipAddress field"
        Pattern = '"ipAddress"\s*:'
    },
    [PSCustomObject]@{
        Name = "database password variable"
        Pattern = 'EDUSCOPE_DB_PASSWORD'
    },
    [PSCustomObject]@{
        Name = "private key"
        Pattern = '-----BEGIN [A-Z ]*PRIVATE KEY-----'
    },
    [PSCustomObject]@{
        Name = "Windows absolute path"
        Pattern = '[A-Za-z]:\\\\'
    }
)

foreach ($check in $forbiddenChecks) {
    if (
        [regex]::IsMatch(
            $rawJson,
            $check.Pattern,
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
        )
    ) {
        throw (
            "Sensitive data check failed: {0}" -f
            $check.Name
        )
    }
}

Write-Host "Sensitive data check: PASS"

$largestFile = $file

if ($storageMode -eq "SHARDED_V1") {
    foreach ($shardFile in $shardFiles) {
        if ($shardFile.Length -gt $largestFile.Length) {
            $largestFile = $shardFile
        }
    }
}

if ($largestFile.Length -ge 100MB) {
    throw ("Snapshot file is 100 MB or larger: {0}" -f $largestFile.FullName)
}
elseif ($largestFile.Length -ge 50MB) {
    Write-Warning ("Snapshot file is 50 MB or larger: {0}" -f $largestFile.FullName)
}
else {
    Write-Host ("Repository file-size check: PASS (largest={0:N2} MB)" -f ($largestFile.Length / 1MB))
}

Write-Host ""
Write-Host "Snapshot validation: PASS"
