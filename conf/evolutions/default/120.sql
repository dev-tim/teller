# --- !Ups
ALTER TABLE EVENT_ATTENDEE ADD COLUMN OPT_OUT TINYINT(1) NOT NULL DEFAULT 0 AFTER ROLE;

# --- !Downs
ALTER TABLE EVENT_ATTENDEE DROP COLUMN OPT_OUT;