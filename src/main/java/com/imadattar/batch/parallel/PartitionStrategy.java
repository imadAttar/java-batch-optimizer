package com.imadattar.batch.parallel;

/**
 * Stratégie de partitionnement des données pour le traitement parallèle.
 *
 * @author Imad ATTAR
 * @since 1.0.0
 */
public enum PartitionStrategy {

    /**
     * Partitionnement statique : chaque thread reçoit un nombre fixe de chunks.
     * Recommandé pour : Traitement homogène (temps de traitement similaire par item).
     */
    STATIC,

    /**
     * Partitionnement dynamique (work-stealing) : les threads "volent" du travail
     * aux threads inactifs.
     * Recommandé pour : Traitement hétérogène (temps de traitement variable).
     */
    DYNAMIC
}
