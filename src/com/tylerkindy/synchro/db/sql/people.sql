-- :name insert-person :<! :1
INSERT INTO people (plan_id, name)
VALUES (:plan-id, :name)
RETURNING id;

-- :name update-person :! :n
UPDATE people
SET name = :name
WHERE id = :id;

-- :name upsert-person-dates :! :n
INSERT INTO people_dates (person_id, date, state)
VALUES :tuple*:people-dates
ON CONFLICT (person_id, date)
DO UPDATE SET state = excluded.state;

-- :name get-people :? :*
SELECT id, name
FROM people
WHERE plan_id = :plan-id
ORDER BY id ASC;

-- :name get-people-dates :? :*
SELECT pd.person_id, pd.date, pd.state
FROM people_dates pd
JOIN people p ON pd.person_id = p.id
WHERE p.plan_id = :plan-id;
