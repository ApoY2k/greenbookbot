ALTER TABLE IF EXISTS public.favs
    ADD COLUMN used bigint NOT NULL DEFAULT 0;
