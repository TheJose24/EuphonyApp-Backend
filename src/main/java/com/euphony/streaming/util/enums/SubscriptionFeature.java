package com.euphony.streaming.util.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SubscriptionFeature {
    PREMIUM_ACCOUNT("1 cuenta premium"),
    TWO_ACCOUNTS("2 cuentas premium"),
    FOUR_ACCOUNTS("Hasta 4 cuentas premium"),
    CANCEL_ANYTIME("Cancela en cualquier momento"),
    HIGH_QUALITY("Calidad de audio alta"),
    OFFLINE_MODE("Modo sin conexión"),
    NO_ADS("Sin publicidad"),
    LYRICS("Letras de canciones"),
    EXCLUSIVE_CONTENT("Contenido exclusivo"),
    CUSTOM_PLAYLISTS("Listas de reproducción personalizadas");

    private final String description;
}
