# Script de Auditoría de Clean Architecture
Write-Host "🏗️ Verificando fronteras de Clean Architecture en el proyecto..." -ForegroundColor Cyan

$violations = 0

# 1. Verificar que Domain no importe Data o DAOs
Write-Host "`n1. Inspeccionando capa Domain (Aislamiento Puro)..." -ForegroundColor Cyan
$domainFiles = Get-ChildItem -Path "app\src\main\java\com\example\tortilleriaderek\domain" -Recurse -Filter "*.kt"
foreach ($file in $domainFiles) {
    $leak = Get-Content $file.FullName | Select-String -Pattern 'import com\.example\.tortilleriaderek\.data\.(local\.dao|local\.entity|security)'
    if ($leak) {
        Write-Host "❌ FUGA EN DOMINIO: $($file.Name) importa clases de Data:" -ForegroundColor Red
        $leak | ForEach-Object { Write-Host "   $_" -ForegroundColor Yellow }
        $violations++
    }
}

# 2. Verificar que ViewModels no importen DAOs directamente
Write-Host "`n2. Inspeccionando capa Presentation (Bypass de DAOs en ViewModels)..." -ForegroundColor Cyan
$vmFiles = Get-ChildItem -Path "app\src\main\java\com\example\tortilleriaderek\presentation" -Recurse -Filter "*ViewModel.kt"
foreach ($file in $vmFiles) {
    $daoImport = Get-Content $file.FullName | Select-String -Pattern 'import com\.example\.tortilleriaderek\.data\.local\.dao\.'
    if ($daoImport) {
        Write-Host "⚠️ BYPASS DE ARQUITECTURA: $($file.Name) inyecta/importa DAOs directamente:" -ForegroundColor Yellow
        $daoImport | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
        $violations++
    }
}

if ($violations -gt 0) {
    Write-Host "`n⚠️ Se detectaron $violations infracciones arquitectónicas a subsanar en el backlog." -ForegroundColor Yellow
} else {
    Write-Host "`n✅ Arquitectura 100% limpia y desacoplada." -ForegroundColor Green
}
