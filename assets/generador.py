import os

def generar_mapa_tmx(nombre_archivo, ancho=100, alto=100):
    # --- Lógica de la Capa 1 (Tiles de fondo) ---
    capa1_filas = []
    for y in range(alto):
        fila = []
        for x in range(ancho):
            # Bordes exteriores vacíos (ID 0)
            if y == 0 or y == alto - 1 or x == 0 or x == ancho - 1:
                fila.append("0")
            # Fila superior del marco (Tiles 1, 8, 7)
            elif y == 1:
                if x == 1: fila.append("1")
                elif x == ancho - 2: fila.append("7")
                else: fila.append("8")
            # Fila inferior del marco (Tiles 3, 4, 5)
            elif y == alto - 2:
                if x == 1: fila.append("3")
                elif x == ancho - 2: fila.append("5")
                else: fila.append("4")
            # Filas intermedias (Tiles 2, 9, 6)
            else:
                if x == 1: fila.append("2")
                elif x == ancho - 2: fila.append("6")
                else: fila.append("9")
        capa1_filas.append(",".join(fila))

    data_capa1 = ",\n".join(capa1_filas)

    # --- Lógica de la Capa 2 (Colisiones - ID 10) ---
    capa2_filas = []
    for y in range(alto):
        fila = []
        for x in range(ancho):
            # Colisión en todo el borde (primeras 2 filas, últimas 2, y columnas laterales)
            if y <= 1 or y >= alto - 2 or x == 0 or x == ancho - 1:
                fila.append("10")
            else:
                fila.append("0")
        capa2_filas.append(",".join(fila))

    data_capa2 = ",\n".join(capa2_filas)

    # --- Estructura del XML ---
    tmx_content = f"""<?xml version="1.0" encoding="UTF-8"?>
<map version="1.10" tiledversion="1.11.2" orientation="orthogonal" renderorder="right-up" width="{ancho}" height="{alto}" tilewidth="16" tileheight="16" infinite="0" nextlayerid="3" nextobjectid="1">
 <tileset firstgid="1" source="background.tsx"/>
 <tileset firstgid="10" source="collision.tsx"/>
 <layer id="1" name="Tile Layer 1" width="{ancho}" height="{alto}">
  <data encoding="csv">
{data_capa1}
</data>
 </layer>
 <layer id="2" name="collisions" width="{ancho}" height="{alto}">
  <data encoding="csv">
{data_capa2}
</data>
 </layer>
</map>"""

    with open(nombre_archivo, "w", encoding="utf-8") as f:
        f.write(tmx_content)
    print(f"¡Mapa '{nombre_archivo}' de {ancho}x{alto} generado con éxito!")

# Ejecutar la función
generar_mapa_tmx("mapa_100x100.tmx", 100, 100)
