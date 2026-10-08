-- Script pour corriger la colonne source_id obsolète dans instance_dependance
-- Cette colonne n'est plus utilisée car on utilise maintenant source_tache_id et source_groupe_tache_id

-- 1. Supprimer les contraintes de clé étrangère qui pourraient référencer source_id
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT constraint_name
        FROM information_schema.table_constraints
        WHERE table_name = 'instance_dependance'
        AND constraint_type = 'FOREIGN KEY'
        AND constraint_name LIKE '%source_id%'
    ) LOOP
        EXECUTE 'ALTER TABLE instance_dependance DROP CONSTRAINT IF EXISTS ' || quote_ident(r.constraint_name);
        RAISE NOTICE 'Contrainte supprimée: %', r.constraint_name;
    END LOOP;
END $$;

-- 2. Supprimer la colonne source_id si elle existe (elle n'est plus utilisée)
DO $$
BEGIN
    -- Vérifier si la colonne existe
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'instance_dependance' 
        AND column_name = 'source_id'
    ) THEN
        -- Supprimer la colonne
        ALTER TABLE instance_dependance DROP COLUMN source_id;
        RAISE NOTICE 'Colonne source_id supprimée avec succès';
    ELSE
        RAISE NOTICE 'La colonne source_id n''existe pas, aucune action nécessaire';
    END IF;
END $$;
