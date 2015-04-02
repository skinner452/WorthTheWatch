CREATE TABLE IF NOT EXISTS Team (
	id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(45) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Game (
	id INT NOT NULL AUTO_INCREMENT,
    date LONG NOT NULL,
    home_score INT,
    away_score INT,
    home_id INT NOT NULL,
    away_id INT NOT NULL,
    tv VARCHAR(100) DEFAULT 'MLS LIVE',
    stadium VARCHAR(45),
    playoffs BOOLEAN DEFAULT false,
    week INT NOT NULL,
    FOREIGN KEY (home_id)
    REFERENCES Team(id),
    FOREIGN KEY (away_id)
    REFERENCES Team(id),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Rating (
	rating INT NOT NULL,
    device_id VARCHAR(45),
    user_name VARCHAR(45) DEFAULT 'Anonymous',
    chars VARCHAR(200),
    game_id INT NOT NULL,
    FOREIGN KEY (game_id)
    REFERENCES Game(id)
);

INSERT INTO Team (name) VALUES ("Sporting KC");
INSERT INTO Team (name) VALUES ("Seattle Sounders");

INSERT INTO Game (home_id, away_id, home_score, away_score, date) VALUES (1, 2, 4, 1, 21371283);

INSERT INTO Rating (rating, device_id, chars, game_id) VALUES (9, 'f7da87f', 'Chippy;Good Crowd', 1);

SELECT home.name, away.name, rating, chars, playoffs, user_name FROM Rating
JOIN Game ON game_id = Game.id
JOIN Team home ON home_id = home.id
JOIN Team away ON away_id = away.id;

SELECT * FROM Game;

SELECT id FROM Team WHERE name = 'Sporting KC';

SELECT * FROM Rating
JOIN Game ON game_id = Game.id
JOIN Team home ON home_id = home.id
JOIN Team away ON away_id = away.id;

SELECT Game.id, home.name, away.name, date FROM Game
JOIN Team home ON home_id = home.id
JOIN Team away ON away_id = away.id
ORDER BY date ASC;

SELECT device_id,game_id FROM Rating WHERE device_id = 'e7e16360e292f03b' AND game_id = 28;

UPDATE Game SET home_score = null WHERE id = 4;

DELETE FROM Game;
DELETE FROM Rating;
DELETE FROM Team;