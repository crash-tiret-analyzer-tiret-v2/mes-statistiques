/* SHARK BOT - Moteur d'analyse statistique N9 */
(function () {
  "use strict";

  function analyser(historique) {
    const valeurs = Array.isArray(historique)
      ? historique.filter(v =>
          typeof v === "number" &&
          Number.isFinite(v) && v > 0
        )
      : [];

    if (valeurs.length === 0) {
      return {
        statut: "DONNEES INSUFFISANTES",
        total: 0,
        moyenne: null,
        mediane: null,
        tendance: "Indéterminée",
        incertitude: "Non calculable",
        message: "Ajoutez des données historiques."
      };
    }

    const triees = [...valeurs].sort((a, b) => a - b);
    const n = valeurs.length;
    const moyenne =
      valeurs.reduce((s, v) => s + v, 0) / n;

    const milieu = Math.floor(n / 2);
    const mediane = n % 2
      ? triees[milieu]
      : (triees[milieu - 1] + triees[milieu]) / 2;

    const recentes = valeurs.slice(-10);
    const moyenneRecente =
      recentes.reduce((s, v) => s + v, 0) / recentes.length;

    let tendance = "Stable dans les observations";

    if (n >= 5) {
      if (moyenneRecente > moyenne * 1.1) {
        tendance = "Moyenne récente supérieure";
      } else if (moyenneRecente < moyenne * 0.9) {
        tendance = "Moyenne récente inférieure";
      }
    }

    const variance =
      valeurs.reduce((s, v) =>
        s + Math.pow(v - moyenne, 2), 0) / n;

    return {
      statut: n >= 30
        ? "HISTORIQUE EXPLOITABLE"
        : "HISTORIQUE EN CONSTRUCTION",
      total: n,
      moyenne: Number(moyenne.toFixed(2)),
      mediane: Number(mediane.toFixed(2)),
      minimum: triees[0],
      maximum: triees[n - 1],
      moyenneRecente: Number(moyenneRecente.toFixed(2)),
      ecartType: Number(Math.sqrt(variance).toFixed(2)),
      tendance,
      incertitude: n < 30
        ? "Élevée : historique limité"
        : "À évaluer selon la variabilité",
      message: "Analyse descriptive du passé. Aucun résultat futur n'est garanti."
    };
  }

  window.SharkBot = { analyser };
})();
