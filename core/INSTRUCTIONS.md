# Protocolo de IA para Juego libGDX (Estilo Brotato)

## Roles y Modelos
- **Planificador (Gemini 1.5 Flash):** Analiza la arquitectura de clases y el ciclo de vida de libGDX.
- **Ejecutor (Llama 3 / Minimax):** Escribe el código Java, respeta el tipado estricto y usa las colecciones de libGDX (ej. Array en lugar de ArrayList).

## Reglas de Implementación
1. **Fase de Diseño:** El Planificador debe explicar qué métodos de `render`, `update` o `dispose` se verán afectados.
2. **Fase de Código:** El Ejecutor implementa los cambios basándose en el diseño anterior.
3. **Memoria:** Siempre verificar que los recursos (Textures, Sound, Batch) se liberen en el `dispose()`.
4. **Optimización:** La meta del proyecto es tener una estructura profesional centrada en la escalabilidad y rendimiento.

