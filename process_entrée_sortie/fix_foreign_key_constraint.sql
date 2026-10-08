-- Script pour corriger la contrainte de clé étrangère de la table tache_agent_assignation
-- Cette contrainte doit référencer instance_groupe_tache au lieu de instance_tache

-- 1. Supprimer toutes les contraintes de clé étrangère existantes sur cette table
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT constraint_name
        FROM information_schema.table_constraints
        WHERE table_name = 'tache_agent_assignation'
        AND constraint_type = 'FOREIGN KEY'
    ) LOOP
        EXECUTE 'ALTER TABLE tache_agent_assignation DROP CONSTRAINT IF EXISTS ' || quote_ident(r.constraint_name);
        RAISE NOTICE 'Contrainte supprimée: %', r.constraint_name;
    END LOOP;
END $$;

-- 2. Vérifier et supprimer l'ancienne colonne tache_id si elle existe encore
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'tache_agent_assignation' 
        AND column_name = 'tache_id'
    ) THEN
        ALTER TABLE tache_agent_assignation DROP COLUMN tache_id;
        RAISE NOTICE 'Colonne tache_id supprimée';
    END IF;
END $$;

-- 3. S'assurer que la colonne groupe_tache_id existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'tache_agent_assignation' 
        AND column_name = 'groupe_tache_id'
    ) THEN
        ALTER TABLE tache_agent_assignation 
        ADD COLUMN groupe_tache_id UUID;
        RAISE NOTICE 'Colonne groupe_tache_id ajoutée';
    END IF;
END $$;

-- 4. Créer la nouvelle contrainte de clé étrangère qui référence instance_groupe_tache
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE table_name = 'tache_agent_assignation' 
        AND constraint_name = 'fk_tache_agent_assignation_groupe_tache'
    ) THEN
        ALTER TABLE tache_agent_assignation
        ADD CONSTRAINT fk_tache_agent_assignation_groupe_tache
        FOREIGN KEY (groupe_tache_id)
        REFERENCES instance_groupe_tache(id)
        ON DELETE CASCADE;
        
        RAISE NOTICE 'Contrainte de clé étrangère créée avec succès';
    ELSE
        RAISE NOTICE 'La contrainte existe déjà';
    END IF;
END $$;
