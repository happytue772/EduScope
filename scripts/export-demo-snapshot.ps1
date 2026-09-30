param(
    [string]$BaseUrl = "https://eduscope-frontend.vercel.app",
    [string]$OutputFile = ".\\eduscope-frontend\\public\\demo-snapshot\\snapshot.json",
    [string]$LoginId = "demo_admin"
)

$ErrorActionPreference = "Stop"

function Normalize-ApiKey {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $uri = [System.Uri]::new(
        [System.Uri]::new($BaseUrl),
        $Path
    )

    if ([string]::IsNullOrWhiteSpace($uri.Query)) {
        return $uri.AbsolutePath
    }

    $pairs = @()

    foreach ($part in $uri.Query.TrimStart('?').Split('&')) {
        if ([string]::IsNullOrWhiteSpace($part)) {
            continue
        }

        $split = $part.Split('=', 2)

        $name = [System.Uri]::UnescapeDataString($split[0])

        $value = if ($split.Length -gt 1) {
            [System.Uri]::UnescapeDataString($split[1])
        }
        else {
            ""
        }

        $pairs += [PSCustomObject]@{
            Name = $name
            Value = $value
        }
    }

    $sorted = $pairs | Sort-Object Name, Value

    $query = (
        $sorted |
        ForEach-Object {
            $encodedName = [System.Uri]::EscapeDataString($_.Name)
            $encodedValue = [System.Uri]::EscapeDataString($_.Value)
            "$encodedName=$encodedValue"
        }
    ) -join "&"

    if ([string]::IsNullOrWhiteSpace($query)) {
        return $uri.AbsolutePath
    }

    return $uri.AbsolutePath + "?" + $query
}

function Get-ApiJson {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    Write-Host "GET $Path"

    return Invoke-RestMethod -Uri ($BaseUrl.TrimEnd('/') + $Path) -Method Get -WebSession $script:Session -Headers @{ Accept = "application/json" }
}

function Save-Response {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        $Data
    )

    $key = Normalize-ApiKey -Path $Path
    $script:Responses[$key] = $Data
}

Write-Host ""
Write-Host "EduScope 실제 API Snapshot Export"
Write-Host "Base URL: $BaseUrl"
Write-Host ""

$securePassword = Read-Host "Login password for $LoginId" -AsSecureString

$password = [System.Net.NetworkCredential]::new(
    "",
    $securePassword
).Password

$script:Session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

$csrf = Invoke-RestMethod -Uri ($BaseUrl.TrimEnd('/') + "/api/auth/csrf") -Method Get -WebSession $script:Session -Headers @{ Accept = "application/json" }

$loginBody = @{
    username = $LoginId
    password = $password
}

$loginBody[$csrf.parameterName] = $csrf.token

Invoke-WebRequest -Uri ($BaseUrl.TrimEnd('/') + "/api/auth/login") -Method Post -WebSession $script:Session -ContentType "application/x-www-form-urlencoded" -Body $loginBody -Headers @{ Accept = "application/json" } | Out-Null

Remove-Variable password

Write-Host "Login success."

$script:Responses = @{}
$studentIndexMap = @{}

$datasets = Get-ApiJson -Path "/api/datasets"
Save-Response -Path "/api/datasets" -Data $datasets

foreach ($dataset in @($datasets)) {
    $datasetId = $dataset.datasetId

    if ($null -eq $datasetId) {
        continue
    }

    $paths = @(
        "/api/dashboard/summary?datasetId=$datasetId",
        "/api/analysis/data-quality/summary?datasetId=$datasetId",
        "/api/analysis-job-overview?datasetId=$datasetId",
        "/api/dataset-details/$datasetId"
    )

    foreach ($path in $paths) {
        try {
            $data = Get-ApiJson -Path $path
            Save-Response -Path $path -Data $data
        }
        catch {
            Write-Warning ("Skip: " + $path + " / " + $_.Exception.Message)
        }
    }
}

$courses = Get-ApiJson -Path "/api/courses"
Save-Response -Path "/api/courses" -Data $courses

foreach ($course in @($courses)) {
    $coursePresentationId = $course.coursePresentationId

    if ($null -eq $coursePresentationId) {
        continue
    }

    $analysisPaths = @(
        "/api/course-analysis/$coursePresentationId",
        "/api/assessment-analysis/$coursePresentationId",
        "/api/activity-analysis/$coursePresentationId",
        "/api/registration-analysis/$coursePresentationId",
        "/api/result-analysis/$coursePresentationId"
    )

    foreach ($path in $analysisPaths) {
        try {
            $data = Get-ApiJson -Path $path
            Save-Response -Path $path -Data $data
        }
        catch {
            Write-Warning ("Skip: " + $path + " / " + $_.Exception.Message)
        }
    }

    try {
        $offset = 0
        $pageSize = 500

        while ($true) {
            $studentSearchPath = "/api/student-analysis/search?coursePresentationId=$coursePresentationId&offset=$offset&limit=$pageSize"

            $studentRows = @(
                Get-ApiJson -Path $studentSearchPath
            )

            foreach ($row in $studentRows) {
                if ($null -eq $row.studentCourseId) {
                    continue
                }

                $studentIndexMap[[string]$row.studentCourseId] = $row

                $studentDetailPath = "/api/student-analysis/$($row.studentCourseId)"

                try {
                    $studentDetail =
                        Get-ApiJson -Path $studentDetailPath

                    Save-Response -Path $studentDetailPath -Data $studentDetail
                }
                catch {
                    Write-Warning "Skip student detail: $studentDetailPath"
                }
            }

            if ($studentRows.Count -lt $pageSize) {
                break
            }

            $offset += $pageSize
        }
    }
    catch {
        Write-Warning "Skip student index for course $coursePresentationId"
    }
}

$studentSearchIndex =
    @($studentIndexMap.Values) |
    Sort-Object sourceStudentId, coursePresentationId

$snapshot = [ordered]@{
    metadata = [ordered]@{
        exportedAtUtc = [DateTime]::UtcNow.ToString("o")
        source = "EduScope deployed API backed by Oracle Autonomous DB"
        mode = "READ_ONLY_SNAPSHOT"
        note = "실제 API 응답을 저장한 공개용 읽기 전용 Snapshot"
    }
    responses = $script:Responses
    studentSearchIndex = $studentSearchIndex
}

$fullOutputPath = [System.IO.Path]::GetFullPath($OutputFile)
$outputDirectory = [System.IO.Path]::GetDirectoryName($fullOutputPath)

New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

$json = $snapshot | ConvertTo-Json -Depth 100

[System.IO.File]::WriteAllText(
    $fullOutputPath,
    $json,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host ""
Write-Host "Snapshot created:"
Write-Host $fullOutputPath
Write-Host ""
Write-Host ("Responses: " + $script:Responses.Count)
Write-Host ("Student index rows: " + $studentSearchIndex.Count)
