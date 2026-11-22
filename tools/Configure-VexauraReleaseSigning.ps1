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

# PowerShell 5.1 treats some native stderr messages as terminating errors even
# when the command succeeds. Capture exit codes without displaying credentials.
function Invoke-GhText([string]$arguments) {
    $info = New-Object System.Diagnostics.ProcessStartInfo
    $info.FileName = $gh.Source
    $info.Arguments = $arguments
    $info.UseShellExecute = $false
    $info.RedirectStandardInput = $false
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    $info.CreateNoWindow = $true
    $process = [System.Diagnostics.Process]::Start($info)
    try {
        $output = $process.StandardOutput.ReadToEnd()
        $errorOutput = $process.StandardError.ReadToEnd()
        if (-not $process.WaitForExit(30000)) {
            $process.Kill()
            throw 'GitHub CLI timed out.'
        }
        if ($process.ExitCode -ne 0) {
            throw ('GitHub CLI command failed: ' + $arguments + ' (exit ' + $process.ExitCode + ') ' + $errorOutput.Trim())
        }
        return $output.Trim()
    } finally {
        $process.Dispose()
    }
}

$activeLogin = Invoke-GhText 'api user --jq .login'
if ($activeLogin -ne 'ctrl-mietze') {
    throw ('Active GitHub account is not ctrl-mietze: ' + $activeLogin)
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

# Immediately start the previously prepared v0.2.0 release from main.
# An absent tag is created by GitHub Actions only after APK signature checks.
# The workflow refuses to change/replace an existing published release.
Invoke-GhText "workflow run vexaura-android.yml -R $repo --ref main" | Out-Null
Write-Output 'Vexaura signed release workflow triggered on main.'
Write-Output 'Follow progress: https://github.com/ctrl-mietze/Vexaura/actions/workflows/vexaura-android.yml'
Write-Output 'Once checks pass, APK appears at https://github.com/ctrl-mietze/Vexaura/releases'

