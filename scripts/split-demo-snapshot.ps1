param(
    [string]$SnapshotFile = "eduscope-frontend\\public\\demo-snapshot\\snapshot.json",
    [int]$ShardSize = 500
)

$ErrorActionPreference = "Stop"

if ($ShardSize -lt 1 -or $ShardSize -gt 1000) {
    throw "ShardSize must be between 1 and 1000."
}

$repoRoot = Split-Path -Parent $PSScriptRoot

if ([System.IO.Path]::IsPathRooted($SnapshotFile)) {
    $fullSnapshotPath = $SnapshotFile
}
else {
    $fullSnapshotPath = Join-Path $repoRoot $SnapshotFile
}

if (-not (Test-Path -LiteralPath $fullSnapshotPath -PathType Leaf)) {
    throw ("Snapshot file not found: {0}" -f $fullSnapshotPath)
}

Write-Host ""
Write-Host "EduScope Snapshot Split"
Write-Host ("Source: {0}" -f $fullSnapshotPath)
Write-Host ("Shard size: {0}" -f $ShardSize)
Write-Host ""

$rawJson = [System.IO.File]::ReadAllText(
    $fullSnapshotPath,
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

$responseProperties = @($snapshot.responses.PSObject.Properties)

$studentDetailProperties = @(
    $responseProperties |
    Where-Object {
        $_.Name -match '^/api/student-analysis/[0-9]+$'
    } |
    Sort-Object {
        [long]($_.Name.Substring("/api/student-analysis/".Length))
    }
)

$studentDetailCount = $studentDetailProperties.Count
$studentIndexRows = @($snapshot.studentSearchIndex)

if ($studentDetailCount -ne $studentIndexRows.Count) {
    throw ("Student detail/index count mismatch before split. details={0}, index={1}" -f $studentDetailCount, $studentIndexRows.Count)
}

$snapshotDirectory = [System.IO.Path]::GetDirectoryName($fullSnapshotPath)
$studentDirectory = Join-Path $snapshotDirectory "students"

New-Item -ItemType Directory -Path $studentDirectory -Force | Out-Null

Get-ChildItem -LiteralPath $studentDirectory -Filter "page-*.json" -File -ErrorAction SilentlyContinue | Remove-Item -Force

$coreResponses = [ordered]@{}

foreach ($property in $responseProperties) {
    if ($property.Name -notmatch '^/api/student-analysis/[0-9]+$') {
        $coreResponses[$property.Name] = $property.Value
    }
}

$studentRowById = @{}

foreach ($row in $studentIndexRows) {
    if ($null -eq $row.studentCourseId) {
        throw "studentSearchIndex contains a row without studentCourseId."
    }

    $studentRowById[[string]$row.studentCourseId] = $row
}

$shardCount = [int][Math]::Ceiling($studentDetailCount / [double]$ShardSize)

for ($shardIndex = 0; $shardIndex -lt $shardCount; $shardIndex++) {
    $start = $shardIndex * $ShardSize
    $endExclusive = [Math]::Min($start + $ShardSize, $studentDetailCount)
    $pageNumber = $shardIndex + 1
    $fileName = "page-{0:D3}.json" -f $pageNumber
    $relativePath = "students/{0}" -f $fileName
    $fullShardPath = Join-Path $studentDirectory $fileName
    $shardResponses = [ordered]@{}

    for ($i = $start; $i -lt $endExclusive; $i++) {
        $property = $studentDetailProperties[$i]
        $shardResponses[$property.Name] = $property.Value

        $studentCourseId = $property.Name.Substring("/api/student-analysis/".Length)

        if (-not $studentRowById.ContainsKey($studentCourseId)) {
            throw ("Student index row not found for studentCourseId={0}" -f $studentCourseId)
        }

        $studentRow = $studentRowById[$studentCourseId]
        $studentRow | Add-Member -NotePropertyName "snapshotShard" -NotePropertyValue $relativePath -Force
    }

    $shardDocument = [ordered]@{
        metadata = [ordered]@{
            storageMode = "SHARDED_V1"
            page = $pageNumber
            shardSize = $ShardSize
            rowCount = $shardResponses.Count
        }
        responses = $shardResponses
    }

    $shardJson = ConvertTo-Json -InputObject $shardDocument -Depth 50

    [System.IO.File]::WriteAllText(
        $fullShardPath,
        $shardJson,
        [System.Text.UTF8Encoding]::new($false)
    )

    Write-Host ("Shard {0}/{1}: {2} rows -> {3}" -f $pageNumber, $shardCount, $shardResponses.Count, $relativePath)
}

$originalMetadata = $snapshot.metadata

if ($null -eq $originalMetadata) {
    $originalMetadata = [PSCustomObject]@{}
}

$originalMetadata | Add-Member -NotePropertyName "storageMode" -NotePropertyValue "SHARDED_V1" -Force
$originalMetadata | Add-Member -NotePropertyName "studentShardSize" -NotePropertyValue $ShardSize -Force
$originalMetadata | Add-Member -NotePropertyName "studentShardCount" -NotePropertyValue $shardCount -Force
$originalMetadata | Add-Member -NotePropertyName "studentDetailCount" -NotePropertyValue $studentDetailCount -Force

$coreDocument = [ordered]@{
    metadata = $originalMetadata
    responses = $coreResponses
    studentSearchIndex = $studentIndexRows
}

$coreJson = ConvertTo-Json -InputObject $coreDocument -Depth 50

[System.IO.File]::WriteAllText(
    $fullSnapshotPath,
    $coreJson,
    [System.Text.UTF8Encoding]::new($false)
)

$coreFile = Get-Item -LiteralPath $fullSnapshotPath
$largestShard = Get-ChildItem -LiteralPath $studentDirectory -Filter "page-*.json" -File | Sort-Object Length -Descending | Select-Object -First 1

Write-Host ""
Write-Host "Snapshot split complete."
Write-Host ("Core responses: {0}" -f $coreResponses.Count)
Write-Host ("Student details: {0}" -f $studentDetailCount)
Write-Host ("Shard count: {0}" -f $shardCount)
Write-Host ("Core size: {0:N2} MB" -f ($coreFile.Length / 1MB))

if ($null -ne $largestShard) {
    Write-Host ("Largest shard: {0:N2} MB ({1})" -f ($largestShard.Length / 1MB), $largestShard.Name)
}
