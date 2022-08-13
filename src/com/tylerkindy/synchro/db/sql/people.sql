-- :name insert-person :<!
INSERT INTO people (plan_id, name)
VALUES (:plan-id, :name)
RETURNING id;

-- :name insert-person-dates :! :n
INSERT INTO people_dates (person_id, date, state)
VALUES :tuple*:people-dates;
