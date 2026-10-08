-- Mise à jour de la contrainte CHECK pour inclure le type "formulaire"
-- Supprimer l'ancienne contrainte
ALTER TABLE template_tache DROP CONSTRAINT IF EXISTS template_tache_type_check;

-- Recréer la contrainte avec les nouvelles valeurs autorisées
ALTER TABLE template_tache 
ADD CONSTRAINT template_tache_type_check 
CHECK (type IN ('tache', 'groupeTache', 'tache_et_groupeTache', 'formulaire', 'FORMULAIRE'));
