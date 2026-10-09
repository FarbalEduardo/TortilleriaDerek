# Script de Auditoria de Calidad y Pruebas
Write-Host "Ejecutando Quality Gate de Tortilleria Derek..." -ForegroundColor Cyan

# 1. Compilacion
Write-Host "`n1. Verificando compilacion Kotlin..." -ForegroundColor Cyan
./gradlew compileDebugKotlin
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error de compilacion Kotlin." -ForegroundColor Red
    exit 1
}

# 2. Pruebas Unitarias
Write-Host "`n2. Ejecutando suite completa de pruebas unitarias..." -ForegroundColor Cyan
./gradlew testDebugUnitTest
if ($LASTEXITCODE -ne 0) {
    Write-Host "Pruebas unitarias fallidas." -ForegroundColor Red
    exit 1
}

# 3. Limite de lineas en UI
Write-Host "`n3. Verificando tamanos de pantalla (*Screen.kt)..." -ForegroundColor Cyan
$violaciones = 0
$screens = Get-ChildItem -Path "app\src\main\java\com\example\tortilleriaderek\ui\screens" -Filter "*Screen.kt"
foreach ($s in $screens) {
    $lines = (Get-Content $s.FullName).Count
    if ($lines -gt 400) {
        Write-Host "Screen gigante detectada: $($s.Name) ($lines lineas)" -ForegroundColor Yellow
        $violaciones = $violaciones + 1
    } else {
        Write-Host "Screen OK: $($s.Name) ($lines lineas)" -ForegroundColor Green
    }
}

Write-Host "`nSuite de pruebas en VERDE. Violaciones de tamano detectadas: $violaciones" -ForegroundColor Green
exit 0
