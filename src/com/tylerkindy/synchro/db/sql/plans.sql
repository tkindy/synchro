-- :name insert-plan :! :n
INSERT INTO plans (id, description)
VALUES (:id, :description);

-- :name insert-plan-dates :! :n
INSERT INTO plan_dates (plan_id, date)
VALUES :tuple*:dates;

-- :name get-plan :? :1
SELECT * FROM plans
WHERE id = :id;

-- :name get-plan-dates :? :*
SELECT date FROM plan_dates
WHERE plan_id = :id;
