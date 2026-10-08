-- Script pour mettre à jour le codeStatut du premier statut (Initialisation) à 'en_attente'
-- Date: 2026-01-12

-- Mettre à jour le codeStatut du statut "Initialisation" pour qu'il soit 'en_attente'
-- Cela permet aux processus créés avec ce statut d'apparaître dans le dashboard
UPDATE statut_processus
SET code_statut = 'en_attente'
WHERE lib_statut = 'Initialisation'
  AND code_statut != 'en_attente';

-- Vérification
DO $$
DECLARE
    count_updated INTEGER;
BEGIN
    SELECT COUNT(*) INTO count_updated
    FROM statut_processus
    WHERE lib_statut = 'Initialisation' AND code_statut = 'en_attente';
    
    IF count_updated > 0 THEN
        RAISE NOTICE 'Mise à jour réussie: % statut(s) "Initialisation" avec codeStatut = ''en_attente''', count_updated;
    ELSE
        RAISE WARNING 'Aucun statut "Initialisation" trouvé avec codeStatut = ''en_attente''';
    END IF;
END $$;
