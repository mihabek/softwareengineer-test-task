select *
from ratings
where created_at > ?
  and created_at <= ?
order by created_at desc
