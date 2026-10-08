-- Script de migration pour déplacer etatAgent de agent_affectation vers agent_personnel
-- Date: 2026-01-12

-- Étape 1: Ajouter la colonne etat_agent à agent_personnel
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agent_personnel' AND column_name = 'etat_agent'
    ) THEN
        ALTER TABLE agent_personnel 
        ADD COLUMN etat_agent VARCHAR(50);
        
        RAISE NOTICE 'Colonne etat_agent ajoutée à agent_personnel';
    ELSE
        RAISE NOTICE 'Colonne etat_agent existe déjà dans agent_personnel';
    END IF;
END $$;

-- Étape 2: Migrer les données existantes depuis agent_affectation vers agent_personnel
-- Pour chaque agent, on prend l'état de sa dernière affectation active (la plus récente)
-- Si un agent n'a pas d'affectation, on met 'entree' par défaut
UPDATE agent_personnel ap
SET etat_agent = COALESCE(
    (
        SELECT aa.etat_agent
        FROM agent_affectation aa
        WHERE aa.agent_id = ap.id
        ORDER BY aa.id DESC
        LIMIT 1
    ),
    'entree'  -- État par défaut si pas d'affectation
)
WHERE ap.etat_agent IS NULL;

RAISE NOTICE 'Données migrées depuis agent_affectation vers agent_personnel';

-- Étape 3: Mettre la colonne etat_agent en NOT NULL après la migration
DO $$
BEGIN
    -- Vérifier qu'il n'y a pas de valeurs NULL
    IF EXISTS (SELECT 1 FROM agent_personnel WHERE etat_agent IS NULL) THEN
        -- S'il y a des NULL, les remplacer par 'entree'
        UPDATE agent_personnel SET etat_agent = 'entree' WHERE etat_agent IS NULL;
        RAISE NOTICE 'Valeurs NULL remplacées par ''entree''';
    END IF;
    
    -- Ajouter la contrainte NOT NULL
    ALTER TABLE agent_personnel 
    ALTER COLUMN etat_agent SET NOT NULL;
    
    RAISE NOTICE 'Contrainte NOT NULL ajoutée à etat_agent';
END $$;

-- Étape 4: Retirer les contraintes de clé étrangère qui pourraient référencer etat_agent dans agent_affectation
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN (
        SELECT tc.constraint_name
        FROM information_schema.table_constraints AS tc
        JOIN information_schema.key_column_usage AS kcu
        ON tc.constraint_name = kcu.constraint_name
        WHERE tc.table_name = 'agent_affectation' 
        AND kcu.column_name = 'etat_agent' 
        AND tc.constraint_type = 'FOREIGN KEY'
    )
    LOOP
        EXECUTE 'ALTER TABLE agent_affectation DROP CONSTRAINT ' || constraint_name;
        RAISE NOTICE 'Contrainte de clé étrangère supprimée: %', constraint_name;
    END LOOP;
END $$;

-- Étape 5: Retirer la colonne etat_agent de agent_affectation
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'agent_affectation' AND column_name = 'etat_agent'
    ) THEN
        ALTER TABLE agent_affectation 
        DROP COLUMN etat_agent;
        
        RAISE NOTICE 'Colonne etat_agent supprimée de agent_affectation';
    ELSE
        RAISE NOTICE 'Colonne etat_agent n''existe pas dans agent_affectation';
    END IF;
END $$;

-- Vérification finale
DO $$
DECLARE
    count_agent_personnel INTEGER;
    count_agent_affectation INTEGER;
BEGIN
    SELECT COUNT(*) INTO count_agent_personnel
    FROM information_schema.columns 
    WHERE table_name = 'agent_personnel' AND column_name = 'etat_agent';
    
    SELECT COUNT(*) INTO count_agent_affectation
    FROM information_schema.columns 
    WHERE table_name = 'agent_affectation' AND column_name = 'etat_agent';
    
    IF count_agent_personnel = 1 AND count_agent_affectation = 0 THEN
        RAISE NOTICE 'Migration réussie: etat_agent est maintenant dans agent_personnel';
    ELSE
        RAISE WARNING 'Vérification: agent_personnel.etat_agent existe: %, agent_affectation.etat_agent existe: %', 
            count_agent_personnel, count_agent_affectation;
    END IF;
END $$;
