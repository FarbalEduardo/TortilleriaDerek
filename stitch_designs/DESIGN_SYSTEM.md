# Design System: Maíz & Masa POS (Stitch)

## Colores Oficiales

```yaml
Primario: '#8D4B00' / '#D97706' (Maíz Dorado - Botones de cobro, totales, selecciones activas)
Secundario: '#AC3400' / '#C2410C' (Terracota - Indicadores numéricos, báscula, alertas)
Terciario: '#2A674C' / '#2D6A4F' (Verde Agave - Pagos completados, estatus OK)
Fondo: '#FFF8F5' (Crema claro natural)
Superficie / Tarjetas: '#FFFFFF' (Blanco puro con bordes tenues)
Bordes / Outline: '#887364' / '#E7E0D6'
Texto Principal: '#1F1B17' (Obsidiana / Carbón de alta legibilidad)
Texto Secundario: '#554336'
```

## Tipografía

- **Familia principal**: `Plus Jakarta Sans`
- **Números / Precios**: Cifras tabulares (`tnum`) para evitar saltos al actualizar peso de báscula en tiempo real.
- **Escalas**:
  - `display-lg`: 40px, bold (800)
  - `headline-lg`: 28px, bold (700)
  - `headline-md`: 22px, bold (700)
  - `headline-sm`: 18px, semi-bold (600)
  - `body-lg`: 16px, medium (500)
  - `body-md`: 14px, regular (400)
  - `label-lg`: 15px, bold (700)
  - `numeric-keypad`: 26px, bold (700)

## Componentes y Ergonomía Táctil
- **Touch Target mínimo**: 56px (óptimo 64px) para uso ágil con dedos con harina o agua.
- **Botón principal de cobro**: Fondo ámbar dorado, texto blanco, altura mínima de 56px.
- **Tarjetas de productos**: Altura mínima de 90px con soporte para toque directo (tap-to-add).
- **Selector rápido de pesos**: Pastillas redondeadas (½ kg, 1 kg, 2 kg, etc.).
- **Bordes redondeados**: 12dp a 16dp para tarjetas y contenedores (`rounded-xl` / `rounded-2xl`).
