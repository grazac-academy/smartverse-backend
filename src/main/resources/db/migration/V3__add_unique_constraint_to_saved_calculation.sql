-- Clean up any existing duplicate saved calculations for the same user, keeping the latest one
DELETE FROM saved_calculation sc1
WHERE EXISTS (
    SELECT 1 FROM saved_calculation sc2
    WHERE sc1.user_id = sc2.user_id
      AND sc1.calculation_id = sc2.calculation_id
      AND (sc1.created_at < sc2.created_at OR (sc1.created_at = sc2.created_at AND sc1.id < sc2.id))
);

-- Add unique constraint on (user_id, calculation_id)
ALTER TABLE saved_calculation
ADD CONSTRAINT uq_saved_calculation_user_calc UNIQUE (user_id, calculation_id);
