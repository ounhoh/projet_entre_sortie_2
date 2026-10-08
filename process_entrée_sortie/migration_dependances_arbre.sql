-- Migration vers la structure en arbre pour les dépendances
-- Structure : 1 tâche source → N tâches cibles

-- ============================================
-- 1. Créer les nouvelles tables de jointure
-- ============================================

-- Table pour les cibles des dépendances d'instance
CREATE TABLE IF NOT EXISTS instance_dependance_cibles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dependance_id UUID NOT NULL,
    cible_tache_id UUID NOT NULL,
    FOREIGN KEY (dependance_id) REFERENCES instance_dependance(id) ON DELETE CASCADE,
    FOREIGN KEY (cible_tache_id) REFERENCES instance_tache(id) ON DELETE CASCADE,
    UNIQUE(dependance_id, cible_tache_id)
);

-- Table pour les cibles des dépendances de template
CREATE TABLE IF NOT EXISTS template_dependance_cibles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dependance_id UUID NOT NULL,
    cible_tache_id UUID NOT NULL,
    FOREIGN KEY (dependance_id) REFERENCES template_dependance(id) ON DELETE CASCADE,
    FOREIGN KEY (cible_tache_id) REFERENCES template_tache(id) ON DELETE CASCADE,
    UNIQUE(dependance_id, cible_tache_id)
);

-- ============================================
-- 2. Ajouter dependance_id dans les tables de tâches
-- ============================================

-- Ajouter dependance_id dans instance_tache
ALTER TABLE instance_tache 
ADD COLUMN IF NOT EXISTS dependance_id UUID;

-- Ajouter dependance_id dans template_tache
ALTER TABLE template_tache 
ADD COLUMN IF NOT EXISTS dependance_id UUID;

-- ============================================
-- 3. Migrer les données existantes (si nécessaire)
-- ============================================
-- NOTE: Cette migration est complexe car elle doit regrouper les dépendances par source.
-- Si vous avez des données existantes, vous devrez peut-être les recréer manuellement.

-- Exemple de migration pour instance_dependance :
-- 1. Grouper les dépendances par source_tache_id
-- 2. Créer une nouvelle dépendance par source avec toutes ses cibles
-- 3. Mettre à jour les tâches cibles avec le dependance_id

-- ============================================
-- 4. Modifier la structure de instance_dependance
-- ============================================

-- Supprimer les anciennes colonnes (ATTENTION: sauvegardez vos données d'abord!)
ALTER TABLE instance_dependance DROP COLUMN IF EXISTS source_type;
ALTER TABLE instance_dependance DROP COLUMN IF EXISTS source_groupe_tache_id;
ALTER TABLE instance_dependance DROP COLUMN IF EXISTS cible_type;
ALTER TABLE instance_dependance DROP COLUMN IF EXISTS cible_tache_id;
ALTER TABLE instance_dependance DROP COLUMN IF EXISTS cible_groupe_tache_id;

-- Ajouter la nouvelle colonne source_tache_id (si elle n'existe pas déjà)
ALTER TABLE instance_dependance 
ADD COLUMN IF NOT EXISTS source_tache_id UUID;

-- Rendre source_tache_id NOT NULL (après migration des données)
-- Note: Décommentez cette ligne après avoir migré toutes les données existantes
-- ALTER TABLE instance_dependance ALTER COLUMN source_tache_id SET NOT NULL;

-- ============================================
-- 5. Modifier la structure de template_dependance
-- ============================================

-- Supprimer les anciennes colonnes (ATTENTION: sauvegardez vos données d'abord!)
ALTER TABLE template_dependance DROP COLUMN IF EXISTS source_type;
ALTER TABLE template_dependance DROP COLUMN IF EXISTS source_groupe_tache_id;
ALTER TABLE template_dependance DROP COLUMN IF EXISTS cible_type;
ALTER TABLE template_dependance DROP COLUMN IF EXISTS cible_tache_id;
ALTER TABLE template_dependance DROP COLUMN IF EXISTS cible_groupe_tache_id;

-- Ajouter la nouvelle colonne source_tache_id (si elle n'existe pas déjà)
ALTER TABLE template_dependance 
ADD COLUMN IF NOT EXISTS source_tache_id UUID;

-- Rendre source_tache_id NOT NULL (après migration des données)
-- Note: Décommentez cette ligne après avoir migré toutes les données existantes
-- ALTER TABLE template_dependance ALTER COLUMN source_tache_id SET NOT NULL;

-- ============================================
-- 6. Ajouter les index pour améliorer les performances
-- ============================================

CREATE INDEX IF NOT EXISTS idx_instance_dependance_source_tache 
ON instance_dependance(source_tache_id);

CREATE INDEX IF NOT EXISTS idx_instance_dependance_cibles_dependance 
ON instance_dependance_cibles(dependance_id);

CREATE INDEX IF NOT EXISTS idx_instance_dependance_cibles_cible 
ON instance_dependance_cibles(cible_tache_id);

CREATE INDEX IF NOT EXISTS idx_instance_tache_dependance 
ON instance_tache(dependance_id);

CREATE INDEX IF NOT EXISTS idx_template_dependance_source_tache 
ON template_dependance(source_tache_id);

CREATE INDEX IF NOT EXISTS idx_template_dependance_cibles_dependance 
ON template_dependance_cibles(dependance_id);

CREATE INDEX IF NOT EXISTS idx_template_dependance_cibles_cible 
ON template_dependance_cibles(cible_tache_id);

CREATE INDEX IF NOT EXISTS idx_template_tache_dependance 
ON template_tache(dependance_id);

-- ============================================
-- NOTES IMPORTANTES
-- ============================================
-- 1. Sauvegardez votre base de données avant d'exécuter ce script
-- 2. Les colonnes DROP COLUMN sont maintenant actives - elles supprimeront les anciennes colonnes
-- 3. Si vous avez des données existantes dans les anciennes colonnes, migrez-les d'abord
-- 4. Testez ce script sur une base de données de développement d'abord
-- 5. Après avoir exécuté ce script, vous pouvez décommenter les lignes ALTER COLUMN ... SET NOT NULL
--    pour rendre source_tache_id obligatoire (une fois que toutes les dépendances ont été migrées)