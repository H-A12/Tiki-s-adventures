package com.tikisadventure.combat.weapons;

//Interfaz para modificar un arma entera antes de usarla
public interface WeaponModifier {
    void apply(Weapon weapon);
}
