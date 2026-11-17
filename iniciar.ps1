# Ejecutar desde PowerShell: .\iniciar.ps1
# Compatible con Windows PowerShell 5.1 y PowerShell 7.
$ErrorActionPreference = 'Stop'

try {
    $proyecto = $PSScriptRoot
    Set-Location -LiteralPath $proyecto

    $archivoEnv = Join-Path $proyecto '.env'
    if (-not (Test-Path -LiteralPath $archivoEnv -PathType Leaf)) {
        throw "No existe .env en la raiz del proyecto. Crea el archivo .env antes de iniciar."
    }

    $requeridas = @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET')
    $valores = @{}
    $lineas = [System.IO.File]::ReadAllLines($archivoEnv)

    foreach ($linea in $lineas) {
        $texto = $linea.Trim()
        if ($texto.Length -eq 0 -or $texto.StartsWith('#')) { continue }

        $posicion = $texto.IndexOf('=')
        if ($posicion -le 0) {
            throw "Formato invalido en .env: usa NOMBRE=valor (sin la linea de secretos en el error)."
        }

        $clave = $texto.Substring(0, $posicion).Trim()
        $valor = $texto.Substring($posicion + 1).Trim()

        if ($requeridas -notcontains $clave) {
            throw "Variable no reconocida en .env: $clave"
        }
        if ($valores.ContainsKey($clave)) {
            throw "Variable duplicada en .env: $clave"
        }

        # Permite valores opcionalmente rodeados por comillas simples o dobles.
        if ($valor.Length -ge 2) {
            $primero = $valor.Substring(0, 1)
            $ultimo = $valor.Substring($valor.Length - 1, 1)
            if (($primero -eq '"' -and $ultimo -eq '"') -or
                ($primero -eq "'" -and $ultimo -eq "'")) {
                $valor = $valor.Substring(1, $valor.Length - 2)
            }
        }
        $valores[$clave] = $valor
    }

    foreach ($clave in $requeridas) {
        if (-not $valores.ContainsKey($clave) -or [string]::IsNullOrWhiteSpace($valores[$clave])) {
            throw "Falta la variable $clave en .env o esta vacia."
        }
    }

    if ($valores['DB_PASSWORD'] -eq 'REEMPLAZAR_POR_TU_PASSWORD') {
        throw 'Debes reemplazar DB_PASSWORD en .env con la contrasena actual de PostgreSQL.'
    }

    # La primera vez genera una clave JWT aleatoria y la guarda en .env.
    # Al guardarla, no cambia en ejecuciones posteriores.
    if ($valores['JWT_SECRET'] -eq 'GENERAR_AUTOMATICAMENTE') {
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $bytes = New-Object byte[] 48
            $rng.GetBytes($bytes)
            $valores['JWT_SECRET'] = [Convert]::ToBase64String($bytes)
        }
        finally {
            $rng.Dispose()
        }

        $nuevasLineas = foreach ($linea in $lineas) {
            if ($linea.Trim() -match '^JWT_SECRET\s*=') {
                'JWT_SECRET=' + $valores['JWT_SECRET']
            }
            else { $linea }
        }
        $utf8SinBOM = [System.Text.UTF8Encoding]::new($false)
        [System.IO.File]::WriteAllLines($archivoEnv, [string[]]$nuevasLineas, $utf8SinBOM)
        Write-Host 'Se creo y guardo una clave JWT local en .env (no se muestra).' -ForegroundColor Green
    }

    if ($valores['JWT_SECRET'].Length -lt 32) {
        throw 'JWT_SECRET es demasiado corto; usa una clave aleatoria de al menos 32 caracteres.'
    }

    foreach ($clave in $requeridas) {
        [System.Environment]::SetEnvironmentVariable($clave, $valores[$clave], 'Process')
    }

    $mavenWrapper = Join-Path $proyecto 'mvnw.cmd'
    if (-not (Test-Path -LiteralPath $mavenWrapper -PathType Leaf)) {
        throw 'No se encontro mvnw.cmd. Coloca iniciar.ps1 en la raiz de Cobranzasprueba.'
    }

    Write-Host 'Configuracion cargada desde .env. Iniciando Spring Boot...' -ForegroundColor Green
    & $mavenWrapper 'spring-boot:run'

    if ($LASTEXITCODE -ne 0) {
        throw "Spring Boot termino con codigo $LASTEXITCODE. Revisa el error de Maven arriba."
    }
}
catch {
    Write-Host ('Error al iniciar: ' + $_.Exception.Message) -ForegroundColor Red
    exit 1
}
