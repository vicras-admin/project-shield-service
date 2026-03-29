select *
from phases p
where 1=1
and organization_id = '00000000-0001-4000-8000-000000000001'
;

select *
from teams t
where 1=1
;

select *
from organizations
where 1=1
;

select m.id, clerk_user_id, m.first_name, m.last_name, email, m.role_id, r.name
from members m
join roles r on m.role_id = r.id
where 1=1
and email like 'mikeraver%'
;

