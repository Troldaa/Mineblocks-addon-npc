package cz.miniomega.mobs.health;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobHealth {
    public int maxHealth;
    public int health;

    public MobHealth(Object dummy, int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        this.health = maxHealth;
    }

    public void reset() {
        health = maxHealth;
    }

    public void setHealth(int health) {
        this.health = Math.min(health, maxHealth);
    }

    public void decrement() {
        health--;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
    }

}
