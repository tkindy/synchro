-- :name insert-person :<!
INSERT INTO people (plan_id, name)
VALUES (:plan-id, :name)
RETURNING id;

-- :name insert-person-dates :! :n
INSERT INTO people_dates (person_id, date, state)
VALUES :tuple*:people-dates;

-- :name get-people :? :*
SELECT id, name
FROM people
WHERE plan_id = :plan-id;

-- :name get-people-dates :? :*
SELECT pd.person_id, pd.date, pd.state
FROM people_dates pd
JOIN people p ON pd.person_id = p.id
WHERE p.plan_id = :plan-id;
