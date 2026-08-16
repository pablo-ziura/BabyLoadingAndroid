# Guía de diseño — Baby Loading Android

Este documento es la fuente de verdad para todo texto de producto, componentes y widgets.

## Tipografía

Toda interfaz propia usa **Nunito Sans**, incluida la app y el widget. El origen es el repositorio oficial Google Fonts/Nunito Sans; la versión queda fijada al commit [`058bd7a2f33d6ad5ef1df985b3db403622016a8c`](https://github.com/googlefonts/NunitoSans/tree/058bd7a2f33d6ad5ef1df985b3db403622016a8c), bajo SIL Open Font License 1.1. Sus hashes SHA-256 son:

- Romana: `f934d7142fb4784bf828da485b7dcbd90c0c80d514e9d49a5da0ed3a1ae2491d`
- Cursiva: `d9d5db18f3c11221a4fbb553cbc709391c1179964c7eaa4466ef43c78aa4492f`

Los dos ficheros viven en `app/src/main/res/font/`; la licencia se distribuye como `res/raw/nunito_sans_ofl.txt`. No se añaden fuentes descargables ni fuentes del sistema para contenido propio.

| Uso | Peso | Rol Material 3 recomendado |
| --- | --- | --- |
| Texto largo y ayuda | 400 Regular | `bodyLarge`, `bodyMedium`, `bodySmall` |
| Texto secundario y metadatos | 400 Regular | `bodySmall` |
| Botones, controles y etiquetas | 600 SemiBold | `labelLarge`, `labelMedium`, `labelSmall` |
| Títulos de sección y pantalla | 700 Bold | `title*`, `headline*` |
| Cifras o hitos protagonistas | 800 ExtraBold | `display*` solo cuando la jerarquía lo justifique |

## Cómo construir una pantalla

- Envuelve siempre el contenido en `BabyLoadingTheme` y usa `MaterialTheme.typography`; no declares `FontFamily`, `TextStyle` ni tamaños locales para texto normal.
- Usa el rol por significado, no por el tamaño que parece en una captura. Conserva los tamaños, alturas de línea y espaciados del sistema de tipos existente.
- Para énfasis editorial usa una cursiva real: `style.copy(fontStyle = FontStyle.Italic)`. No uses slant sintético, fuentes cursivas del sistema ni una segunda familia.
- Da preferencia a `bodyLarge` para texto informativo de embarazo y permite varias líneas. Solo limita líneas cuando el diseño necesita una etiqueta breve.
- Mantén contraste legible con los colores de `MaterialTheme.colorScheme`, objetivos táctiles Material 3 y texto escalable en `sp`. No resuelvas una falta de contraste aumentando el peso.
- No uses ExtraBold fuera de una cifra o hito principal, ni más de dos pesos en el mismo componente. Medium (500) se reserva para metadatos compactos; no sustituye a SemiBold en controles ni a Bold en títulos.
- Los iconos `ImageVector`, emoji y controles del sistema no son texto de marca y conservan su representación nativa.

## Equivalencias y ejemplos

| Propósito | Android Material 3 | iOS SwiftUI | Peso |
| --- | --- | --- | --- |
| Título de pantalla | `headlineLarge` | `.largeTitle` | 700 Bold |
| Título de componente | `titleLarge` | `.title3` | 700 Bold |
| Cuerpo informativo | `bodyLarge` | `.body` | 400 Regular |
| Control o acción | `labelLarge` | `.headline` | 600 SemiBold |
| Metadato | `bodySmall` | `.caption` | 400 Regular |
| Cifra protagonista | `displaySmall` | `widget(size:weight:)` | 800 ExtraBold |

Pantalla:

```kotlin
Text(
    text = stringResource(R.string.dashboard_title),
    style = MaterialTheme.typography.headlineLarge,
)
```

Componente reutilizable:

```kotlin
Text(
    text = stringResource(R.string.save),
    style = MaterialTheme.typography.labelLarge,
)
```

Widget: asigna el propósito equivalente antes de crear el `Text` de Glance: `displaySmall` para la semana o contador principal, `labelMedium` para su etiqueta y `bodySmall` para apoyo. El widget no crea una segunda familia ni peso local.

Énfasis editorial:

```kotlin
Text(
    text = stringResource(R.string.editorial_note),
    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
)
```

## Correspondencia con iOS

Android e iOS comparten Nunito Sans y la jerarquía Regular → SemiBold → Bold → ExtraBold. No deben forzarse tamaños idénticos: Android utiliza roles Material 3 y iOS estilos SwiftUI que escalan con Dynamic Type. Al añadir una pantalla en ambas plataformas, asigna primero el mismo propósito de texto y después el rol nativo equivalente.
