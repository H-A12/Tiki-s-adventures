package effects;

import com.badlogic.gdx.math.Vector2;

public class Casing {
    public Vector2 pos;
    public Vector2 velocity;
    public float rotation;
    public float angularVelocity;
    public float lifeTime;

    // 1. IMPORTANTE: Declarar groundLevel como atributo de clase
    private float groundLevel;

    public Casing(Vector2 startPos, Vector2 startVelocity) {
        this.pos = new Vector2(startPos);
        this.velocity = new Vector2(startVelocity);
        this.rotation = (float) Math.random() * 360;
        this.angularVelocity = (float) (Math.random() * 600 - 300);
        this.lifeTime = 1.5f; // Aumentado un poco para ver el rebote bien

        // 2. Definimos el suelo 0.4 unidades por debajo de donde se creó
        this.groundLevel = startPos.y - 1f;
    }

    public void update(float delta) {
        // 1. Aplicar movimiento
        pos.add(velocity.x * delta, velocity.y * delta);
        rotation += angularVelocity * delta;
        lifeTime -= delta;

        // 2. Gravedad constante
        velocity.y -= 18f * delta;

        // 3. Fricción horizontal (aire)
        velocity.x *= 0.99f;

        // 4. Lógica de Rebote corregida
        if (pos.y < groundLevel) {
            pos.y = groundLevel; // Reposicionar en el suelo

            // Si la caída es fuerte, rebota
            if (Math.abs(velocity.y) > 0.5f) {
                velocity.y *= -0.4f;     // Rebote (pierde 60% fuerza)
                velocity.x *= 0.6f;      // Fricción con el suelo
                angularVelocity *= 0.5f; // Gira más lento al chocar
            } else {
                // Si la velocidad es muy baja, se queda quieto
                velocity.y = 0;
                velocity.x = 0;
                angularVelocity = 0;
            }
        }
    }
}
