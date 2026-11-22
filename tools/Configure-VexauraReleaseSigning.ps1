# One-time Vexaura release-signing setup on the authorized Windows Surface.
# Run only when the GitHub CLI is already authenticated as the repository owner.
# The keystore and its passwords are never printed and are never added to Git.
# Uploads the SAME key that was generated for Vexaura v0.2.0.
$ErrorActionPreference = 'Stop'

$repo = 'ctrl-mietze/Vexaura'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$propsPath = Join-Path $projectRoot 'keystore.properties'

if (-not (Test-Path -LiteralPath $propsPath)) {
    throw 'keystore.properties is missing. Never generate a replacement key if releases already exist.'
}

$props = @{}
foreach ($line in [IO.File]::ReadAllLines($propsPath)) {
    if ($line -match '^\s*([^#!=\s][^=]*)=(.*)$') {
        $props[$matches[1].Trim()] = $matches[2]
    }
}
foreach ($required in @('storeFile', 'storePassword', 'keyAlias', 'keyPassword')) {
    if ([string]::IsNullOrEmpty($props[$required])) {
        throw "Signing property $required is missing."
    }
}

$keyfile = [IO.Path]::GetFullPath((Join-Path $projectRoot $props['storeFile']))
if (-not (Test-Path -LiteralPath $keyfile)) {
    throw 'The original Vexaura signing key was not found. Refusing to create a different one.'
}
$gh = Get-Command gh -ErrorAction Stop
& $gh.Source auth status *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'The GitHub CLI is not authenticated. Sign in using gh auth login first.'
}

$secrets = [ordered]@{
    VEXAURA_KEYSTORE_B64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keyfile))
    VEXAURA_STORE_PASSWORD = $props['storePassword']
    VEXAURA_KEY_ALIAS = $props['keyAlias']
    VEXAURA_KEY_PASSWORD = $props['keyPassword']
}

foreach ($secretName in $secrets.Keys) {
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $gh.Source
    $pinfo.Arguments = "secret set $secretName -R $repo"
    $pinfo.UseShellExecute = $false
    $pinfo.RedirectStandardInput = $true
    $pinfo.RedirectStandardError = $true
    $pinfo.RedirectStandardOutput = $true
    $pinfo.CreateNoWindow = $true

    $proc = [System.Diagnostics.Process]::Start($pinfo)
    try {
        $proc.StandardInput.Write($secrets[$secretName])
        $proc.StandardInput.Close()
        $proc.WaitForExit(30000) | Out-Null
        if (-not $proc.HasExited) {
            $proc.Kill()
            throw "Timed out setting GitHub secret $secretName."
        }
        if ($proc.ExitCode -ne 0) {
            throw "GitHub refused signing secret $secretName."
        }
        Write-Output "Uploaded: $secretName"
    } finally {
        $proc.Dispose()
    }
}

$secrets.Clear()
Write-Output 'Vexaura signing configured. The original key remains stored only on your Surface and encrypted in GitHub Actions Secrets.'
