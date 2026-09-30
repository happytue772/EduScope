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

$studentDetailCount = @(
    $responseProperties |
    Where-Object {
        $_.Name -match '^/api/student-analysis/[0-9]+$'
    }
).Count

$studentIndexCount = @(
    $snapshot.studentSearchIndex
).Count

Write-Host ("Responses: {0}" -f $responseProperties.Count)
Write-Host ("Courses: {0}" -f $courseCount)
Write-Host ("Student details: {0}" -f $studentDetailCount)
Write-Host ("Student index rows: {0}" -f $studentIndexCount)

if ($studentDetailCount -ne $studentIndexCount) {
    throw (
        "Student detail/index count mismatch. details={0}, index={1}" -f
        $studentDetailCount,
        $studentIndexCount
    )
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

if ($file.Length -ge 100MB) {
    Write-Warning "Snapshot is 100 MB or larger. GitHub regular push will be blocked."
}
elseif ($file.Length -ge 50MB) {
    Write-Warning "Snapshot is 50 MB or larger. GitHub will warn and browser loading may be heavy."
}
else {
    Write-Host "Repository file-size check: PASS"
}

Write-Host ""
Write-Host "Snapshot validation: PASS"
