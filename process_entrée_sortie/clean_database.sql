-- Nettoie toutes les tables du schéma public sans les supprimer.
-- Réinitialise aussi les séquences (identités) et gère les FK via CASCADE.
DO $$
DECLARE
  rec RECORD;
BEGIN
  FOR rec IN
    SELECT tablename
    FROM pg_tables
    WHERE schemaname = 'public'
  LOOP
    EXECUTE format('TRUNCATE TABLE %I.%I RESTART IDENTITY CASCADE', 'public', rec.tablename);
  END LOOP;
END $$;
