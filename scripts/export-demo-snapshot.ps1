param(
    [string]$BaseUrl = "https://eduscope-frontend.vercel.app",
    [string]$OutputFile = "eduscope-frontend\\public\\demo-snapshot\\snapshot.json",
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

function Get-Utf8ResponseContent {
    param(
        [Parameter(Mandatory = $true)]
        $Response
    )

    # Windows PowerShell 5.1에서도 API 본문을 UTF-8 기준으로 읽는다.
    if ($null -ne $Response.RawContentStream) {
        try {
            $stream = $Response.RawContentStream

            if ($stream.CanSeek) {
                $stream.Position = 0
            }

            $reader = [System.IO.StreamReader]::new(
                $stream,
                [System.Text.Encoding]::UTF8,
                $true,
                4096,
                $true
            )

            try {
                return $reader.ReadToEnd()
            }
            finally {
                $reader.Dispose()
            }
        }
        catch {
            # RawContentStream을 사용할 수 없는 환경에서는 Content로 fallback한다.
        }
    }

    return [string]$Response.Content
}

function Get-JsonRootKind {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Json
    )

    $trimmed = $Json.TrimStart()

    if ($trimmed.StartsWith("[")) {
        return "ARRAY"
    }

    if ($trimmed.StartsWith("{")) {
        return "OBJECT"
    }

    return "SCALAR"
}

function Get-ApiJsonResponse {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    Write-Host "GET $Path"

    $requestParams = @{
        Uri = $BaseUrl.TrimEnd('/') + $Path
        Method = "Get"
        WebSession = $script:Session
        Headers = @{ Accept = "application/json" }
        UseBasicParsing = $true
    }

    $response = Invoke-WebRequest @requestParams
    $rawJson = Get-Utf8ResponseContent -Response $response

    if ([string]::IsNullOrWhiteSpace($rawJson)) {
        throw "Empty JSON response: $Path"
    }

    try {
        $data = ConvertFrom-Json -InputObject $rawJson
    }
    catch {
        throw "Invalid JSON response: $Path / $($_.Exception.Message)"
    }

    # RawJson은 최종 Snapshot에 그대로 넣어 원래 JSON Array/Object 구조를 보존한다.
    return [PSCustomObject]@{
        Data = $data
        RawJson = $rawJson.Trim()
        RootKind = Get-JsonRootKind -Json $rawJson
    }
}

function Assert-RootKind {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        $Response,

        [Parameter(Mandatory = $true)]
        [ValidateSet("ARRAY", "OBJECT", "SCALAR")]
        [string]$Expected
    )

    if ($Response.RootKind -ne $Expected) {
        throw (
            "Unexpected JSON root for "
            + $Path
            + ". Expected="
            + $Expected
            + ", Actual="
            + $Response.RootKind
        )
    }
}

function Sanitize-PublicResponseJson {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [string]$RawJson
    )

    $result = $RawJson

    # 공개 Snapshot에는 Windows staging 경로를 노출하지 않는다.
    if ($Path.StartsWith("/api/analysis-job-overview")) {
        $result = [regex]::Replace(
            $result,
            '("resultFilePath"\s*:\s*)"(?:\\.|[^"\\])*"',
            '$1null',
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
        )
    }

    return $result
}

function Save-Response {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        $Response
    )

    $key = Normalize-ApiKey -Path $Path

    $script:Responses[$key] =
        Sanitize-PublicResponseJson -Path $Path -RawJson $Response.RawJson
}

function Assert-PublicSnapshotSafety {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Json
    )

    $checks = @(
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

    foreach ($check in $checks) {
        if (
            [regex]::IsMatch(
                $Json,
                $check.Pattern,
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
        ) {
            throw (
                "Public Snapshot safety check failed: "
                + $check.Name
            )
        }
    }
}

Write-Host ""
Write-Host "EduScope API Snapshot Export"
Write-Host "Base URL: $BaseUrl"
Write-Host ""

$securePassword = Read-Host "Login password for $LoginId" -AsSecureString

$password = [System.Net.NetworkCredential]::new(
    "",
    $securePassword
).Password

$script:Session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

$csrfParams = @{
    Uri = $BaseUrl.TrimEnd('/') + "/api/auth/csrf"
    Method = "Get"
    WebSession = $script:Session
    Headers = @{ Accept = "application/json" }
}

$csrf = Invoke-RestMethod @csrfParams

$loginBody = @{
    username = $LoginId
    password = $password
}

$loginBody[$csrf.parameterName] = $csrf.token

$loginParams = @{
    Uri = $BaseUrl.TrimEnd('/') + "/api/auth/login"
    Method = "Post"
    WebSession = $script:Session
    ContentType = "application/x-www-form-urlencoded"
    Body = $loginBody
    Headers = @{ Accept = "application/json" }
    UseBasicParsing = $true
}

Invoke-WebRequest @loginParams | Out-Null

Remove-Variable password

Write-Host "Login success."

$script:Responses = @{}
$studentIndexMap = @{}

$datasetsResponse = Get-ApiJsonResponse -Path "/api/datasets"
Assert-RootKind -Path "/api/datasets" -Response $datasetsResponse -Expected "ARRAY"
Save-Response -Path "/api/datasets" -Response $datasetsResponse

$datasets = @($datasetsResponse.Data)

foreach ($dataset in $datasets) {
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
            $response = Get-ApiJsonResponse -Path $path
            Save-Response -Path $path -Response $response
        }
        catch {
            Write-Warning ("Skip: " + $path + " / " + $_.Exception.Message)
        }
    }
}

$coursesResponse = Get-ApiJsonResponse -Path "/api/courses"
Assert-RootKind -Path "/api/courses" -Response $coursesResponse -Expected "ARRAY"
Save-Response -Path "/api/courses" -Response $coursesResponse

$courses = @($coursesResponse.Data)

foreach ($course in $courses) {
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
            $response = Get-ApiJsonResponse -Path $path
            Save-Response -Path $path -Response $response
        }
        catch {
            Write-Warning ("Skip: " + $path + " / " + $_.Exception.Message)
        }
    }

    try {
        $offset = 0
        $pageSize = 500
        $courseSeenStudentIds = @{}

        while ($true) {
            $studentSearchPath =
                "/api/student-analysis/search"
                + "?coursePresentationId=$coursePresentationId"
                + "&offset=$offset"
                + "&limit=$pageSize"

            $studentPage = Get-ApiJsonResponse -Path $studentSearchPath

            Assert-RootKind -Path $studentSearchPath -Response $studentPage -Expected "ARRAY"

            $studentRows = @($studentPage.Data)
            $rowCount = $studentRows.Count

            if ($rowCount -eq 0) {
                break
            }

            $newRowCount = 0

            foreach ($row in $studentRows) {
                if ($null -eq $row.studentCourseId) {
                    continue
                }

                $studentCourseKey = [string]$row.studentCourseId

                if (-not $courseSeenStudentIds.ContainsKey($studentCourseKey)) {
                    $courseSeenStudentIds[$studentCourseKey] = $true
                    $newRowCount++
                }

                $studentIndexMap[$studentCourseKey] = $row

                $studentDetailPath =
                    "/api/student-analysis/"
                    + $row.studentCourseId

                try {
                    $studentDetail = Get-ApiJsonResponse -Path $studentDetailPath
                    Save-Response -Path $studentDetailPath -Response $studentDetail
                }
                catch {
                    Write-Warning (
                        "Skip student detail: "
                        + $studentDetailPath
                    )
                }
            }

            if ($newRowCount -eq 0) {
                throw (
                    "Student pagination did not advance for course "
                    + $coursePresentationId
                    + ". Check deployed Backend offset/limit support."
                )
            }

            # 실제 반환 건수만큼 이동하여 서버가 limit보다 작은 cap을 사용해도 전체를 순회한다.
            $offset += $rowCount
        }

        Write-Host (
            "Student rows for course "
            + $coursePresentationId
            + ": "
            + $courseSeenStudentIds.Count
        )
    }
    catch {
        throw (
            "Student index export failed for course "
            + $coursePresentationId
            + " / "
            + $_.Exception.Message
        )
    }
}

$studentSearchIndex = @(
    $studentIndexMap.Values |
    Sort-Object sourceStudentId, coursePresentationId
)

$metadata = [ordered]@{
    exportedAtUtc = [DateTime]::UtcNow.ToString("o")
    source = "EduScope deployed API backed by Oracle Autonomous DB"
    mode = "READ_ONLY_SNAPSHOT"
    note = "Public read-only snapshot exported from real API responses"
}

$metadataJson = ConvertTo-Json -InputObject $metadata -Depth 10
$studentIndexJson = ConvertTo-Json -InputObject $studentSearchIndex -Depth 20

$responseEntries = New-Object System.Collections.Generic.List[string]

foreach ($key in ($script:Responses.Keys | Sort-Object)) {
    $jsonKey = ConvertTo-Json -InputObject ([string]$key) -Compress
    $rawValue = [string]$script:Responses[$key]

    $entry =
        "    "
        + $jsonKey
        + ": "
        + $rawValue

    [void]$responseEntries.Add($entry)
}

$responsesJson =
    "{"
    + [Environment]::NewLine
    + ($responseEntries -join ("," + [Environment]::NewLine))
    + [Environment]::NewLine
    + "  }"

$json =
    "{"
    + [Environment]::NewLine
    + '  "metadata": '
    + $metadataJson
    + ","
    + [Environment]::NewLine
    + '  "responses": '
    + $responsesJson
    + ","
    + [Environment]::NewLine
    + '  "studentSearchIndex": '
    + $studentIndexJson
    + [Environment]::NewLine
    + "}"

Assert-PublicSnapshotSafety -Json $json

$repoRoot = Split-Path -Parent $PSScriptRoot

if ([System.IO.Path]::IsPathRooted($OutputFile)) {
    $fullOutputPath = $OutputFile
}
else {
    $fullOutputPath = Join-Path $repoRoot $OutputFile
}

$outputDirectory = [System.IO.Path]::GetDirectoryName($fullOutputPath)

New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

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
Write-Host ("Courses: " + $courses.Count)
Write-Host ("Student index rows: " + $studentSearchIndex.Count)
Write-Host "Safety check: PASS"
