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

function Get-JsonFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $raw = [System.IO.File]::ReadAllText(
        $Path,
        [System.Text.Encoding]::UTF8
    )

    try {
        return ConvertFrom-Json -InputObject $raw
    }
    catch {
        throw ("Invalid JSON: {0} / {1}" -f $Path, $_.Exception.Message)
    }
}

function Test-ResponseKey {
    param(
        [Parameter(Mandatory = $true)]
        $Responses,

        [Parameter(Mandatory = $true)]
        [string]$Key
    )

    return $null -ne (
        $Responses.PSObject.Properties[$Key]
    )
}

function Require-ResponseKey {
    param(
        [Parameter(Mandatory = $true)]
        $Responses,

        [Parameter(Mandatory = $true)]
        [string]$Key,

        [Parameter(Mandatory = $true)]
        [string]$Screen
    )

    if (-not (Test-ResponseKey -Responses $Responses -Key $Key)) {
        throw ("[{0}] missing response: {1}" -f $Screen, $Key)
    }
}

function Write-ScreenPass {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Number,

        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    Write-Host ("[{0}] {1}: PASS" -f $Number, $Name)
}

$snapshot = Get-JsonFile -Path $fullPath

if ($null -eq $snapshot.responses) {
    throw "Snapshot responses object is missing."
}

if ($null -eq $snapshot.studentSearchIndex) {
    throw "Snapshot studentSearchIndex is missing."
}

$responses = $snapshot.responses

Require-ResponseKey -Responses $responses -Key "/api/datasets" -Screen "01/08/09/10"
Require-ResponseKey -Responses $responses -Key "/api/courses" -Screen "02~06"

$datasets = @($responses.PSObject.Properties["/api/datasets"].Value)
$courses = @($responses.PSObject.Properties["/api/courses"].Value)

if ($datasets.Count -eq 0) {
    throw "Snapshot /api/datasets is empty."
}

if ($courses.Count -eq 0) {
    throw "Snapshot /api/courses is empty."
}

foreach ($dataset in $datasets) {
    if ($null -eq $dataset.datasetId) {
        throw "Dataset row without datasetId was found."
    }

    $datasetId = [string]$dataset.datasetId

    Require-ResponseKey -Responses $responses -Key ("/api/dashboard/summary?datasetId={0}" -f $datasetId) -Screen "01 Dashboard"
    Require-ResponseKey -Responses $responses -Key ("/api/dataset-details/{0}" -f $datasetId) -Screen "08 Dataset"
    Require-ResponseKey -Responses $responses -Key ("/api/analysis/data-quality/summary?datasetId={0}" -f $datasetId) -Screen "09 Data Quality"
    Require-ResponseKey -Responses $responses -Key ("/api/analysis-job-overview?datasetId={0}" -f $datasetId) -Screen "10 Analysis Job"
}

Write-ScreenPass -Number "01" -Name "Dashboard"

foreach ($course in $courses) {
    if ($null -eq $course.coursePresentationId) {
        throw "Course row without coursePresentationId was found."
    }

    $courseId = [string]$course.coursePresentationId

    Require-ResponseKey -Responses $responses -Key ("/api/course-analysis/{0}" -f $courseId) -Screen "02 Course Analysis"
    Require-ResponseKey -Responses $responses -Key ("/api/assessment-analysis/{0}" -f $courseId) -Screen "03 Assessment Analysis"
    Require-ResponseKey -Responses $responses -Key ("/api/activity-analysis/{0}" -f $courseId) -Screen "04 Activity Analysis"
    Require-ResponseKey -Responses $responses -Key ("/api/registration-analysis/{0}" -f $courseId) -Screen "05 Registration Analysis"
    Require-ResponseKey -Responses $responses -Key ("/api/result-analysis/{0}" -f $courseId) -Screen "06 Result Analysis"
}

Write-ScreenPass -Number "02" -Name "Course Analysis"
Write-ScreenPass -Number "03" -Name "Assessment Analysis"
Write-ScreenPass -Number "04" -Name "Activity Analysis"
Write-ScreenPass -Number "05" -Name "Registration Analysis"
Write-ScreenPass -Number "06" -Name "Result Analysis"

$studentIndex = @($snapshot.studentSearchIndex)

if ($studentIndex.Count -eq 0) {
    throw "[07 Student Analysis] studentSearchIndex is empty."
}

$storageMode = [string]$snapshot.metadata.storageMode

if ($storageMode -eq "SHARDED_V1") {
    $snapshotDirectory = [System.IO.Path]::GetDirectoryName($fullPath)
    $studentDirectory = Join-Path $snapshotDirectory "students"

    if (-not (Test-Path -LiteralPath $studentDirectory -PathType Container)) {
        throw "[07 Student Analysis] students shard directory is missing."
    }

    $shardResponseKeys = @{}
    $shardFileNames = @{}

    $shardFiles = @(
        Get-ChildItem -LiteralPath $studentDirectory -Filter "page-*.json" -File |
        Sort-Object Name
    )

    if ($shardFiles.Count -eq 0) {
        throw "[07 Student Analysis] no shard files were found."
    }

    foreach ($shardFile in $shardFiles) {
        $shard = Get-JsonFile -Path $shardFile.FullName

        if ($null -eq $shard.responses) {
            throw ("[07 Student Analysis] responses missing in {0}" -f $shardFile.Name)
        }

        $relativePath = "students/{0}" -f $shardFile.Name
        $shardFileNames[$relativePath] = $true

        foreach ($property in @($shard.responses.PSObject.Properties)) {
            $shardResponseKeys[$property.Name] = $relativePath
        }
    }

    foreach ($row in $studentIndex) {
        if ($null -eq $row.studentCourseId) {
            throw "[07 Student Analysis] index row without studentCourseId was found."
        }

        if ([string]::IsNullOrWhiteSpace([string]$row.snapshotShard)) {
            throw ("[07 Student Analysis] snapshotShard missing for studentCourseId={0}" -f $row.studentCourseId)
        }

        $relativeShard = [string]$row.snapshotShard

        if (-not $shardFileNames.ContainsKey($relativeShard)) {
            throw ("[07 Student Analysis] shard file not found for studentCourseId={0}: {1}" -f $row.studentCourseId, $relativeShard)
        }

        $detailKey = "/api/student-analysis/{0}" -f $row.studentCourseId

        if (-not $shardResponseKeys.ContainsKey($detailKey)) {
            throw ("[07 Student Analysis] detail response missing for studentCourseId={0}" -f $row.studentCourseId)
        }

        if ($shardResponseKeys[$detailKey] -ne $relativeShard) {
            throw ("[07 Student Analysis] shard mapping mismatch for studentCourseId={0}" -f $row.studentCourseId)
        }
    }
}
else {
    foreach ($row in $studentIndex) {
        $detailKey = "/api/student-analysis/{0}" -f $row.studentCourseId
        Require-ResponseKey -Responses $responses -Key $detailKey -Screen "07 Student Analysis"
    }
}

Write-ScreenPass -Number "07" -Name "Student Analysis"
Write-ScreenPass -Number "08" -Name "Dataset"
Write-ScreenPass -Number "09" -Name "Data Quality"
Write-ScreenPass -Number "10" -Name "Analysis Job"

Write-Host ""
Write-Host ("Datasets checked: {0}" -f $datasets.Count)
Write-Host ("Courses checked: {0}" -f $courses.Count)
Write-Host ("Student index rows checked: {0}" -f $studentIndex.Count)
Write-Host "Snapshot 01~10 coverage validation: PASS"
