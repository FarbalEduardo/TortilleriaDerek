# Script de Auditoria de Diseno y Modularidad UI
Write-Host "Verificando estandares de Modo de Diseno y Modularidad UI..." -ForegroundColor Cyan

$errorCount = 0

# 1. Verificar tamano de pantallas (*Screen.kt < 400 lineas)
$screens = Get-ChildItem -Path "app\src\main\java\com\example\tortilleriaderek\ui\screens" -Filter "*Screen.kt"
foreach ($screen in $screens) {
    $lines = (Get-Content -Path $screen.FullName).Count
    if ($lines -gt 400) {
        Write-Host "VIOLACION MODULARIDAD: $($screen.Name) tiene $lines lineas (maximo permitido: 400)" -ForegroundColor Red
        $errorCount = $errorCount + 1
    } else {
        Write-Host "OK: $($screen.Name) - $lines lineas" -ForegroundColor Green
    }
}

# 2. Verificar strings quemados en UI
Write-Host "`nVerificando strings literales quemados en Compose..." -ForegroundColor Cyan
$files = Get-ChildItem -Path "app\src\main\java\com\example\tortilleriaderek\ui" -Recurse -Filter "*.kt"
$hardcodedCount = 0
foreach ($f in $files) {
    $matches = Select-String -Path $f.FullName -Pattern 'Text\("[^"]+"\)|text = "[^"]+"'
    if ($matches) {
        $hardcodedCount = $hardcodedCount + $matches.Count
    }
}

if ($hardcodedCount -gt 0) {
    Write-Host "ADVERTENCIA: Se encontraron $hardcodedCount textos literales quemados en la UI." -ForegroundColor Yellow
    Write-Host "Extraer a res/values/strings.xml con stringResource(R.string.xxx)" -ForegroundColor Yellow
} else {
    Write-Host "Cero hardcoding detectado en componentes UI." -ForegroundColor Green
}

if ($errorCount -gt 0) {
    Write-Host "`nEl Design Gate ha fallado con $errorCount violaciones criticas." -ForegroundColor Red
    exit 1
} else {
    Write-Host "`nDesign Gate superado con exito." -ForegroundColor Green
    exit 0
}
