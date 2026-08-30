# Design System — Baby Loading

Este documento es el contrato visual compartido entre las aplicaciones Android e iOS de Baby Loading. Mantén las reglas comunes equivalentes en ambos repositorios; las tablas de implementación pueden nombrar APIs nativas distintas.

## Tipografía

Toda interfaz propia usa **Nunito Sans**, incluida la app y el widget. El origen es el repositorio oficial Google Fonts/Nunito Sans; la versión queda fijada al commit [`058bd7a2f33d6ad5ef1df985b3db403622016a8c`](https://github.com/googlefonts/NunitoSans/tree/058bd7a2f33d6ad5ef1df985b3db403622016a8c), bajo SIL Open Font License 1.1. Sus hashes SHA-256 son:

- Romana: `f934d7142fb4784bf828da485b7dcbd90c0c80d514e9d49a5da0ed3a1ae2491d`
- Cursiva: `d9d5db18f3c11221a4fbb553cbc709391c1179964c7eaa4466ef43c78aa4492f`

Los dos ficheros viven en `core/designsystem/src/main/res/font/`; la licencia se distribuye como `core/designsystem/src/main/res/raw/nunito_sans_ofl.txt`. No se añaden fuentes descargables ni fuentes del sistema para contenido propio.

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
- Los iconos `ImageVector` y los controles del sistema no son texto de marca y conservan su representación nativa.

## Equivalencias y ejemplos

| Propósito | Android Material 3 | iOS SwiftUI | Peso |
| --- | --- | --- | --- |
| Título destacado de Inicio | `headlineLarge` | `.largeTitle` | 700 Bold |
| Título de pantalla de nivel superior | `titleLarge` | `.title2` | 700 Bold |
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

## Principios compartidos

- Diseña por **propósito semántico**, no por una aproximación visual ni por el nombre de un token de una plataforma.
- Un color de marca, uno de selección y uno de estado son conceptos distintos, aunque se perciban parecidos.
- Android e iOS comparten intención, jerarquía, contenido y accesibilidad; cada plataforma conserva sus controles y convenciones nativas.
- El fondo puede extenderse bajo las barras del sistema. El contenido legible, los controles y las imágenes informativas deben respetar sus áreas seguras.
- La app es solo clara. No se introduce modo oscuro ni color dinámico sin actualizar este contrato y ambos clientes.

## Paleta y semántica de color

Los valores hex son la referencia visual para revisión. Implementa los colores con tokens centrales o con la API semántica nativa indicada, nunca con literales repetidos dentro de una pantalla.

| Propósito | Referencia visual | Uso | Android | iOS |
| --- | --- | --- | --- | --- |
| Fondo superior | `#FFBFD1` | Inicio del degradado principal | `BabyGradientTop` | `GradientBackground.gradientTop` |
| Fondo inferior | `#C7B8F5` | Fin del degradado principal | `BabyGradientBottom` | `GradientBackground.gradientBottom` |
| Marca berry | `#9B405C` | Identidad persistente, no selección temporal | `BrandPink` / `colorScheme.primary` | Token de marca central cuando sea necesario |
| Acento de selección rosa | `#FF2D55` | Semana o día actual, selección activa, énfasis puntual | `BabyAccentPink` | `Color.pink` |
| Acento de selección violeta | Púrpura del sistema al 80 % | Extremo final de un gradiente de selección | `BabyAccentPurple.copy(alpha = 0.8f)` | `Color.purple.opacity(0.8)` |
| Superficie de tarjeta actual | `#FFFFFF` | Elemento actual o prioritario | `Color.White` | `Color.white` |
| Superficie de tarjeta secundaria | Blanco al 88 % | Elemento de contexto sobre el degradado | `Color.White.copy(alpha = 0.88f)` | `Color.white.opacity(0.88)` |
| Texto principal | `#211A1C` | Contenido legible de primera jerarquía | `colorScheme.onSurface` | `.primary` |
| Texto secundario | `#504347` | Metadatos y apoyo | `colorScheme.onSurfaceVariant` | `.secondary` |
| Separador y trazo pasivo | Blanco al 30–65 % | Línea temporal o borde no seleccionado sobre degradado | `Color.White.copy(alpha = …)` | `Color.white.opacity(…)` |
| Estado al día | Verde suave | Seguimiento cuya próxima captura aún no vence | `BabyStatusPositiveContainer` | `Color.green.opacity(0.18)` |
| Estado pendiente | Naranja suave | Seguimiento cuya próxima captura ya vence | `BabyStatusAttentionContainer` | `Color.orange.opacity(0.18)` |

### Reglas de color

- `BrandPink` y `Color.pink` no son intercambiables. El primero representa la identidad de Android; el segundo es el acento semántico compartido para una selección actual.
- No uses `colorScheme.primary`, `.tint`, `.accentColor` ni un color de marca como atajo para un propósito que tenga token propio.
- Los gradientes de estado actual usan, de izquierda a derecha, el acento rosa y el acento violeta. No sustituyas el violeta semántico por el lavanda de marca.
- La sombra de una tarjeta actual toma el acento rosa con 15 % de opacidad; las tarjetas no actuales no proyectan sombra de color.
- No se transmite significado solo mediante el color: el estado actual también debe incluir etiqueta, semántica seleccionada o ambos cuando corresponda.

## Espaciado, forma y elevación

La escala base es de 4 puntos. Evita valores locales si existe un token o un componente que exprese la misma relación.

| Token base | Valor | Uso típico |
| --- | --- | --- |
| Extra pequeño | 4 | Separación mínima y badge vertical |
| Pequeño | 8 | Separación entre grupos relacionados |
| Medio | 16 | Gutter, separación entre elementos principales y ancho de línea temporal |
| Grande | 24 | Padding de pantalla y agrupación de secciones |
| Extra grande | 32 | Separación de bloques destacados |

| Elemento compartido | Especificación |
| --- | --- |
| Tarjetas de la línea temporal | Radio continuo de 16; tarjeta actual blanca; tarjeta secundaria blanca al 88 %; sombra rosa al 15 %, radio 8, desplazamiento vertical 4 solo en la actual. |
| Línea temporal | Ancho 16; separación de 16 respecto a la tarjeta; línea blanca al 30 % de 2 puntos; marcadores pasivos blancos al 50–65 %. |
| Marcador de semana actual | Círculo rosa de 14 con borde blanco de 2; semanas no actuales usan círculo blanco al 50 % de 8. |
| Marcador de día actual | Círculo rosa de 7 con borde blanco de 1; los no actuales son de 4. |
| Imagen de tamaño del bebé | Contenedor circular de 40; recorte circular de 34; borde rosa al 20 % de 1.5. |
| Badge «Aquí estás» / «You are here» | Cápsula con gradiente de selección; texto blanco Bold de caption2; padding horizontal 10 y vertical 4. |
| Tarjetas métricas del panel | Las tarjetas de una misma fila alinean icono, cifra y etiqueta en bandas verticales iguales. La etiqueta reserva 48 puntos antes del inset compartido de la tarjeta y puede crecer con el escalado de texto. |
| Aviso de término tardío y postérmino | Reutiliza la superficie secundaria de `BabyLoadingCard` con título Bold, texto explicativo Regular y relación con la fecha estimada de parto. Es informativo, no interactivo, y no reutiliza imagen fetal ni anillo de progreso. |

## Componentes, estados y accesibilidad

- Reutiliza el tema, las formas, las superficies y los tokens del sistema de diseño antes de definir constantes de feature.
- Una tarjeta estática no debe exponerse como botón ni tener chevrons. Si se habilita interacción, documenta el estado, la acción y la semántica en esta guía.
- Una tarjeta actual debe comunicarlo visualmente y mediante accesibilidad. En Android usa `selected`; en iOS el trait `.isSelected` y el valor localizado.
- Las imágenes decorativas se ocultan de accesibilidad. Las imágenes informativas requieren una descripción localizada.
- No uses emoji como texto decorativo. Sustitúyelos por ilustraciones raster originales, sin tintar, con fondo transparente; mantén sus colores pastel, formas suaves y jerarquía secundaria respecto al texto localizado adyacente.
- Respeta Dynamic Type en iOS y el escalado de texto del sistema en Android; no bloquees el tamaño de fuentes en pantallas de app.
- Mantén contraste legible entre texto y superficie. La selección rosa se acompaña de una etiqueta o semántica, nunca solo color.
- Los objetivos interactivos respetan el mínimo recomendado por cada plataforma. Los encabezados se marcan como tales y siguen una jerarquía consistente.
- Para término tardío (41+0 a 41+6) y postérmino (42+0 en adelante), muestra en ambas plataformas el mismo estado informativo: etiqueta de fase y relación con la fecha estimada de parto. El widget usa esta terminología compacta y dirige a revisar la fecha en la app.

## Gobernanza y sincronización entre plataformas

1. Define o revisa primero el propósito visual: color, tipografía, espaciado, forma, estado y accesibilidad.
2. Añade el mapeo Android e iOS en la misma edición. Si una plataforma no tiene todavía token, créalo en su sistema de diseño; no reutilices uno parecido.
3. Implementa con tokens o componentes centrales, nunca con literales de pantalla salvo valores documentados de un componente.
4. Valida la vista en ambas plataformas antes de cerrar el cambio.
5. Mantén `DESIGN.md` equivalente en los dos repositorios. Los cambios de iOS se realizan en una rama propia y los de Android respetan los cambios locales existentes.

Cuando exista una discrepancia entre una captura y el código, consulta el código de la otra plataforma para identificar el propósito antes de ajustar valores. Las capturas sirven para validar; no sustituyen este contrato.
