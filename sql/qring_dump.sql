-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: qring_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `achievement_comment`
--

DROP TABLE IF EXISTS `achievement_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `achievement_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `min_rate` int NOT NULL,
  `max_rate` int NOT NULL,
  `comment_text` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `achievement_comment`
--

LOCK TABLES `achievement_comment` WRITE;
/*!40000 ALTER TABLE `achievement_comment` DISABLE KEYS */;
INSERT INTO `achievement_comment` VALUES (1,0,9,'이제 시작이에요! 첫걸음을 떼어볼까요? ?'),(2,10,30,'더디네요 좀만 더 노력해보세요! ?'),(3,31,50,'좋은 흐름이에요! 절반을 향해 달리는 중 ?'),(4,51,70,'와우, 벌써 이만큼이나 선전하셨군요! 대단해요 ?'),(5,71,99,'이번 주 엄청난 성장을 보여주고 있어요! 이대로 쭉 가보자고요 ?'),(6,100,100,'완벽합니다! 목표를 마스터하셨어요 ?');
/*!40000 ALTER TABLE `achievement_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chapter`
--

DROP TABLE IF EXISTS `chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapter` (
  `chapter_id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_num` int DEFAULT NULL,
  `chapter_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `required_points` int DEFAULT NULL,
  `content_id` bigint NOT NULL,
  PRIMARY KEY (`chapter_id`),
  KEY `FK80tj1ir66aa8pi7xbqdu1pxq4` (`content_id`),
  CONSTRAINT `FK80tj1ir66aa8pi7xbqdu1pxq4` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapter`
--

LOCK TABLES `chapter` WRITE;
/*!40000 ALTER TABLE `chapter` DISABLE KEYS */;
/*!40000 ALTER TABLE `chapter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content`
--

DROP TABLE IF EXISTS `content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content` (
  `content_id` bigint NOT NULL AUTO_INCREMENT,
  `status` varchar(20) DEFAULT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `required_points` int DEFAULT '0',
  `total_quiz_count` int DEFAULT '0',
  `total_chapters` int DEFAULT NULL,
  `level_code` int DEFAULT NULL,
  PRIMARY KEY (`content_id`),
  KEY `FKiudotpe4xsqlggmeaxf52r1rg` (`category_id`),
  KEY `FKc6mnk27ohvqcsjprwtx7i2g63` (`level_code`),
  CONSTRAINT `FKc6mnk27ohvqcsjprwtx7i2g63` FOREIGN KEY (`level_code`) REFERENCES `difficulty_level` (`level_code`),
  CONSTRAINT `FKiudotpe4xsqlggmeaxf52r1rg` FOREIGN KEY (`category_id`) REFERENCES `content_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content`
--

LOCK TABLES `content` WRITE;
/*!40000 ALTER TABLE `content` DISABLE KEYS */;
INSERT INTO `content` VALUES (1,'ACTIVE',NULL,'도서관 좌석번호 64번',1,0,0,NULL,NULL),(2,'ACTIVE',NULL,'3번 관람석',1,0,0,NULL,NULL),(3,'ACTIVE',NULL,'끊어진 전화선',2,0,0,NULL,NULL),(4,'ACTIVE',NULL,'빨간 구두',2,0,0,NULL,NULL),(5,'ACTIVE',NULL,'남자친구가 제 꿈을 예측해요',1,0,0,NULL,NULL),(6,'ACTIVE',NULL,'수학 과외 선생님과 내 여자친구',2,0,0,NULL,NULL);
/*!40000 ALTER TABLE `content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content_category`
--

DROP TABLE IF EXISTS `content_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL,
  `display_order` int DEFAULT NULL,
  `story_count` int DEFAULT '0',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_category`
--

LOCK TABLES `content_category` WRITE;
/*!40000 ALTER TABLE `content_category` DISABLE KEYS */;
INSERT INTO `content_category` VALUES (1,'로맨스',1,3),(2,'스토리',2,3);
/*!40000 ALTER TABLE `content_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `daily_goal_record`
--

DROP TABLE IF EXISTS `daily_goal_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_goal_record` (
  `goal_record_id` bigint NOT NULL AUTO_INCREMENT,
  `achieve_date` date DEFAULT NULL,
  `is_attained` bit(1) DEFAULT NULL,
  `completed_chapter_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`goal_record_id`),
  KEY `FKjwdwgsgfsym6yb1gy8sasn0of` (`completed_chapter_id`),
  KEY `FKl9pqbfiq9mjcdd9utaijf8v2a` (`user_id`),
  CONSTRAINT `FKjwdwgsgfsym6yb1gy8sasn0of` FOREIGN KEY (`completed_chapter_id`) REFERENCES `chapter` (`chapter_id`),
  CONSTRAINT `FKl9pqbfiq9mjcdd9utaijf8v2a` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `daily_goal_record`
--

LOCK TABLES `daily_goal_record` WRITE;
/*!40000 ALTER TABLE `daily_goal_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `daily_goal_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `difficulty_level`
--

DROP TABLE IF EXISTS `difficulty_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `difficulty_level` (
  `level_code` int NOT NULL,
  `level_desc` varchar(255) DEFAULT NULL,
  `level_name` varchar(20) NOT NULL,
  PRIMARY KEY (`level_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `difficulty_level`
--

LOCK TABLES `difficulty_level` WRITE;
/*!40000 ALTER TABLE `difficulty_level` DISABLE KEYS */;
INSERT INTO `difficulty_level` VALUES (1,'기초 영어 단어 수준','쉬움'),(2,'문장 완성 수준','보통'),(3,'문맥 이해 수준','어려움');
/*!40000 ALTER TABLE `difficulty_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_detail`
--

DROP TABLE IF EXISTS `quiz_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_detail` (
  `quiz_id` bigint NOT NULL AUTO_INCREMENT,
  `correct_answer` text,
  `explanation` text,
  `quiz_type` varchar(200) DEFAULT NULL,
  `content_id` bigint NOT NULL,
  `script_id` bigint NOT NULL,
  `question` text,
  `options` json DEFAULT NULL,
  `difficulty` int DEFAULT NULL,
  `acceptable_answers` json DEFAULT NULL,
  `hint` text,
  PRIMARY KEY (`quiz_id`),
  KEY `FKhpjjt22h2je6erxuxsdfqbptw` (`content_id`),
  KEY `FKmj5hwc5l6f7k9s69gnuxr6mpp` (`script_id`),
  CONSTRAINT `FKhpjjt22h2je6erxuxsdfqbptw` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `FKmj5hwc5l6f7k9s69gnuxr6mpp` FOREIGN KEY (`script_id`) REFERENCES `script` (`script_id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_detail`
--

LOCK TABLES `quiz_detail` WRITE;
/*!40000 ALTER TABLE `quiz_detail` DISABLE KEYS */;
INSERT INTO `quiz_detail` VALUES (1,'annoyed','\'annoyed\'는 무언가에 짜증이 나거나 성가실 때 쓰는 표현이야. 내 자리 옆에 누가 앉아서 기분이 별로였던 상황이지.','multiple_choice',1,11,'다음 중 \'짜증나다, 성가시다\'의 영어 표현으로 알맞은 것은?','[\"annoyed\", \"amazed\", \"admired\", \"attended\"]',1,'[\"annoyed\"]','a로 시작하는 7글자 단어'),(2,'left behind','\'leave behind\'는 무언가를 그 자리에 남겨두고 떠난다는 뜻의 구동사야.','subjective',1,11,'\'~을 남겨두다/놓고 가다\' = l____ b______\n\nShe ______ ______ a note on my desk.\n(뜻: 그녀가 내 책상에 쪽지를 남겨두고 갔다)',NULL,2,'[\"left behind\", \"Left behind\"]','떠나다(leave) + 뒤에(behind), 뭔가를 그 자리에 두고 떠날 때 써'),(3,'awkward to start up','\'strike up a conversation\'이나 \'start up a conversation\'은 대화를 시작한다는 뜻이야. 상황이 어색해서 말 걸기 힘들 때 쓰는 표현이지.','subjective',1,11,'\'말 걸기 애매했다\'\n\nShe was so focused on her book that it felt ______ to ______ a conversation.\n(뜻: 대화를 시작하다)',NULL,3,'[\"awkward to start up\", \"awkward to strike up\", \"uncomfortable to start up\", \"uncomfortable to strike up\", \"weird to start\", \"strange to start\"]','s___ u_ (2단어, 엔진을 시동 걸 때도 쓰는 표현)'),(4,'curiosity','\'curiosity\'는 호기심을 뜻해. 쪽지 내용이 궁금해서 생긴 마음이지.','multiple_choice',1,22,'다음 중 \'호기심\'을 뜻하는 영어 단어는?','[\"curiosity\", \"confusion\", \"confidence\", \"consideration\"]',1,'[\"curiosity\"]','c로 시작하는 9글자, 고양이를 죽인다는 속담에 나오는 단어'),(5,'realized','\'realize\'는 깨닫다, 알아차리다라는 뜻이야. 영국식으로는 \'realise\'라고 쓰기도 해.','subjective',1,22,'\'깨닫다\' = r______\n\nI ______ that I had never seen her face properly.\n(뜻: 그녀의 얼굴을 제대로 본 적이 없다는 걸 깨달았다)',NULL,2,'[\"realized\", \"realised\", \"Realized\", \"Realised\"]','re로 시작, \'알아차리다/인식하다\'라는 뜻 💡'),(6,'on purpose','\'on purpose\'는 의도적으로, 일부러라는 뜻이야. deliberately나 intentionally도 같은 의미로 쓸 수 있어.','subjective',1,22,'\'일부러 일찍 가봤다\'\n\nI went to the library ______ ______ to see when she arrived.\n(뜻: 의도적으로, 특별히)',NULL,3,'[\"on purpose\", \"deliberately\", \"intentionally\"]','o_ p_______ (2단어, 목적을 가지고 할 때 쓰는 표현)'),(7,'gather courage','\'gather courage\'는 용기를 모아내다, 용기내다라는 뜻이야. 쪽지에 글을 쓰기 위해 용기를 낸 상황이지.','multiple_choice',1,33,'다음 중 \'용기를 내다\'의 영어 표현으로 알맞은 것은?','[\"gather courage\", \"collect money\", \"receive support\", \"follow advice\"]',1,'[\"gather courage\"]','g로 시작하는 단어와 함께 쓰이는 표현'),(8,'exchanging','\'exchange\'는 서로 주고받다라는 뜻으로, 메시지나 정보를 상호 교환할 때 쓰는 자연스러운 표현이야.','subjective',1,33,'\'메시지를 주고받다\' = e_______ messages\n\nThey started _______ notes from that day on.\n(뜻: 그날부터 쪽지를 주고받기 시작했다)',NULL,2,'[\"exchanging\", \"Exchanging\"]','e로 시작, \'change\'와 비슷한 뜻이지만 서로 주고받는다는 의미'),(9,'exchanging','\'exchange notes\'는 쪽지를 주고받는다는 뜻이야. passing, trading도 비슷한 의미로 쓸 수 있어.','subjective',1,33,'\'쪽지 주고받기\'\n\nWe started _______ _______ notes.\n(뜻: ~을 주고받다)',NULL,3,'[\"exchanging\", \"passing\", \"trading\", \"sharing\"]','e_______ (시소처럼 서로 주고받는 느낌)'),(10,'disappointed','\'disappointed\'는 기대했던 일이 이뤄지지 않아서 허무하고 실망스러울 때 쓰는 표현이야.','multiple_choice',1,44,'다음 중 \'허무한, 실망스러운\'을 뜻하는 영어 표현은?','[\"disappointed\", \"determined\", \"delighted\", \"developed\"]',1,'[\"disappointed\"]','d로 시작하는 11글자 단어'),(11,'hollow','\'hollow\'는 물리적으로 속이 비었다는 뜻뿐만 아니라 감정적으로 공허하고 허무하다는 뜻으로도 쓰여.','subjective',1,44,'\'허무한, 공허한\' = h______\n\nIt felt so ______ to part ways without even seeing each other properly.\n(뜻: 제대로 서로를 보지도 못하고 헤어져서 너무 허무했다)',NULL,2,'[\"hollow\", \"Hollow\"]','h로 시작, \'속이 빈\' 나무를 떠올려봐 🌳'),(12,'empty','\'empty\'는 허무하고 공허한 감정을 표현할 때 쓰여. hollow, disappointed도 비슷한 의미로 사용할 수 있어.','subjective',1,44,'\'허무했다\'는 감정을 영어로 표현해보세요.\n\nI felt so ______.\n(뜻: 공허하고 실망스러운 감정)',NULL,3,'[\"empty\", \"hollow\", \"disappointed\", \"deflated\", \"let down\"]','e_____ (6글자, \'빈\'이라는 뜻도 있는 단어)'),(13,'occasionally','\'occasionally\'는 가끔, 때때로라는 뜻으로 정기적이지 않고 이따금씩 일어나는 상황을 표현해.','multiple_choice',1,57,'다음 중 \'가끔, 때때로\'를 의미하는 영어 표현으로 알맞은 것은?','[\"occasionally\", \"obviously\", \"officially\", \"originally\"]',1,'[\"occasionally\"]','o로 시작하는 부사, \'once in a while\'과 비슷한 뜻'),(14,'occasionally','\'occasionally\'는 \'가끔, 때때로\'라는 뜻으로 빈도를 나타내는 부사야.','subjective',1,57,'\'가끔씩\' = o_________\n\nWe still ________ communicate through notes.\n(뜻: 우리는 아직도 가끔 쪽지로 소통해)',NULL,2,'[\"occasionally\", \"Occasionally\"]','한 번씩, 때때로... o로 시작하는 부사'),(15,'exchange','\'exchange notes\'는 쪽지를 주고받는다는 뜻이야. pass notes, send notes도 자연스러운 표현이고.','subjective',1,57,'\'가끔 쪽지로 대화한다\'\n\nWe still ______ ______ notes sometimes.\n(뜻: 서로 주고받다)',NULL,3,'[\"exchange\", \"pass\", \"send\", \"write\", \"share\"]','e_______'),(16,'occasionally','\'occasionally\'는 가끔씩, 때때로라는 뜻이야. 학생이 가끔 머리를 쥐어뜯는 상황에서 쓸 수 있어.','multiple_choice',2,69,'다음 중 \'가끔, 때때로\'를 뜻하는 영어 표현은?','[\"occasionally\", \"obviously\", \"officially\", \"originally\"]',1,'[\"occasionally\"]','o로 시작하는 12글자 표현'),(17,'scribbling','\'scribble\'은 급히 또는 대충 끄적거리며 쓰는 것을 의미해. 학생이 숙제하며 연필로 끄적거리는 상황에 딱 맞는 표현이야.','subjective',2,69,'\'끄적거리다\' = s______\n\nHe was ______ notes in the margin.\n(뜻: 그는 여백에 끄적거리고 있었다)',NULL,2,'[\"scribbling\", \"Scribbling\", \"scrawling\", \"Scrawling\"]','s로 시작, \'긁다\'라는 뜻도 있어 🐔'),(18,'pulling','\'pulling his hair out\'은 스트레스나 좌절감에 머리를 쥐어뜯는다는 관용적 표현이야. tearing이나 tugging도 비슷한 의미로 쓸 수 있어.','subjective',2,69,'수학 문제가 어려워서 \'머리를 쥐어뜯었다\'\n\nHe was ______ his hair out over the difficult math problems.\n(뜻: 스트레스를 받아 머리를 뜯다)',NULL,3,'[\"pulling\", \"tearing\", \"tugging\"]','p_____ (털이나 머리카락을 뽑는다는 동사)'),(19,'empty','\'empty\'는 텅 비어있다는 뜻이야. 학생이 안 와서 자리가 완전히 비어있는 상황이지.','multiple_choice',2,81,'다음 중 \'완전히 비어 있는\'을 뜻하는 영어 표현은?','[\"empty\", \"early\", \"equal\", \"eager\"]',1,'[\"empty\"]','e로 시작하는 5글자 단어'),(20,'empty','\'empty\'는 아무것도 없이 완전히 비어있는 상태를 나타내는 기본적인 형용사야.','subjective',2,81,'\'텅 빈\' = e______\n\nThe seat was completely ______.\n(뜻: 그 자리는 완전히 비어있었다)',NULL,2,'[\"empty\", \"Empty\"]','e로 시작, \'빈 집\'을 영어로 하면 _____ house'),(21,'glancing','\'glance\'는 빠르고 짧게 힐끗 보는 걸 뜻해. 계속 창가 쪽을 신경 쓰며 보는 상황에 딱 맞아.','subjective',2,81,'\'자꾸 창가를 쳐다봤다\'\n\nYeon-woo kept ______ toward the window.\n(뜻: 힐끗힐끗 보다, 슬쩍 보다)',NULL,3,'[\"glancing\", \"looking\", \"gazing\", \"staring\", \"peeking\"]','g_____ (6글자, 빠르고 짧게 보는 동작)'),(22,'swollen','\'swollen\'은 부은 상태를 나타내는 말이야. 울어서 눈이 부었을 때 쓰지.','multiple_choice',2,93,'다음 중 \'부은, 붓다\'의 영어 표현으로 알맞은 것은?','[\"swollen\", \"sleepy\", \"smooth\", \"serious\"]',1,'[\"swollen\"]','s로 시작하는 단어, 벌에 쏘이면 이렇게 됨'),(23,'swollen','\'swollen\'은 부어오른 상태를 나타내는 형용사야. 울거나 다쳤을 때 자주 쓰이지.','subjective',2,93,'\'부어오른\' = s______\n\nHer eyes were ______ from crying.\n(뜻: 그녀의 눈은 울어서 부어 있었다)',NULL,2,'[\"swollen\", \"Swollen\"]','s로 시작, 벌에 쏘였을 때도 이렇게 돼 🐝'),(24,'collapsed down','\'collapse\'나 \'pass out\'은 갑작스럽게 의식을 잃고 쓰러지는 걸 표현할 때 쓰는 자연스러운 영어야.','subjective',2,93,'\'갑자기 쓰러지다\'\n\nHe suddenly ______ ______.\n(뜻: 의식을 잃고 넘어지다)',NULL,3,'[\"collapsed down\", \"passed out\", \"fell down\", \"fainted away\", \"collapsed\", \"blacked out\"]','c_______ d___ (의식과 관련된 표현, 기절하다)'),(25,'give up','\'give up\'은 포기하다라는 뜻이야. 아버지가 딸에게 수학이 어려워도 포기하지 말라고 격려하는 상황이지.','multiple_choice',2,105,'다음 중 \'포기하다\'의 영어 표현으로 알맞은 것은?','[\"give up\", \"get up\", \"grow up\", \"go up\"]',1,'[\"give up\"]','g로 시작하는 구동사 (give + up)'),(26,'tucked','\'tuck\'은 무언가를 살살 끼워 넣거나 집어넣는다는 뜻이야. \'tucked between\'은 사이에 끼워져 있다는 표현이지.','subjective',2,105,'\'끼워 넣다\' = t______\n\nA letter was ______ between the pages.\n(뜻: 편지가 페이지 사이에 끼워져 있었다)',NULL,2,'[\"tucked\", \"Tucked\"]','t로 시작, \'집어넣다/꽂다\'라는 뜻'),(27,'give up','\'give up\'은 포기하다라는 뜻이야. quit이나 surrender도 같은 의미로 쓸 수 있어.','subjective',2,105,'\'포기하지 마\'\n\nDon\'t ______ ______.\n(뜻: ~을 그만두다, 포기하다)',NULL,3,'[\"give up\", \"quit\", \"surrender\"]','g___ u_ (2단어, 담배를 끊을 때도 쓰는 표현)'),(28,'carefully','\'carefully\'는 주의 깊게, 유심히 관찰하거나 행동할 때 쓰는 부사야. 아버지가 딸의 자리를 신중하게 살펴본 상황이지.','multiple_choice',2,118,'다음 중 \'유심히, 주의 깊게\'를 뜻하는 영어 부사는?','[\"carefully\", \"certainly\", \"currently\", \"completely\"]',1,'[\"carefully\"]','c로 시작하는 9글자 부사'),(29,'inspected','\'inspect\'는 세심하게 관찰하다/살펴보다라는 뜻이야. 아버지가 딸의 자리를 조용히 확인하는 감동적인 장면을 표현할 때 쓸 수 있어.','subjective',2,118,'\'유심히 관찰하다\' = i______\n\nHe ______ the seat #3 carefully and left without saying a word.\n(뜻: 그는 3번 자리를 유심히 살펴보고는 아무 말 없이 떠났다)',NULL,2,'[\"inspected\", \"examined\", \"observed\", \"studied\"]','눈으로 자세히 들여다본다는 뜻, \'in-\'로 시작해'),(30,'gazing intently','\'gaze intently\'는 관심을 가지고 유심히 바라본다는 뜻이야. stare intently, look intently도 같은 의미로 쓸 수 있어.','subjective',2,118,'\'유심히 보다\'\n\nHe was _______ _______ at seat number 3, then left without saying a word.\n(뜻: 관심 있게 자세히 보다)',NULL,3,'[\"gazing intently\", \"staring intently\", \"looking intently\", \"gazing carefully\", \"staring carefully\", \"looking carefully\", \"gazing closely\", \"staring closely\", \"looking closely\"]','g___ at (2단어, 응시하다/뚫어져라 보다)'),(31,'nervous','\'nervous\'는 긴장하거나 불안할 때 쓰는 표현이야. 손에 땀이 날 정도로 긴장한 상황이지.','multiple_choice',3,135,'다음 중 \'긴장한, 초조한\'을 뜻하는 영어 단어는?','[\"nervous\", \"natural\", \"nothing\", \"normal\"]',1,'[\"nervous\"]','n으로 시작하는 7글자, \'신경\'과 관련된 단어'),(32,'night shift','\'night shift\'는 야간 근무, 밤 교대를 뜻하는 표현이야.','subjective',3,135,'\'야간 근무\' = n____ s____\n\nShe\'s been working the ______ ______ for three months.\n(뜻: 밤 근무를 맡고 있다)',NULL,2,'[\"night shift\", \"Night shift\", \"night-shift\", \"nightshift\"]','밤(night)과 교대근무를 뜻하는 s로 시작하는 단어'),(33,'palms','\'palms were getting sweaty\'는 긴장해서 손에 땀이 날 때 쓰는 자연스러운 표현이야. hands도 맞지만 palms가 더 정확해.','subjective',3,135,'\'손에 땀이 났다\'\n\nHer ______ were getting sweaty.\n(긴장하거나 불안할 때 손이 축축해지는 상황)',NULL,3,'[\"palms\", \"hands\"]','p___ (4글자, 손바닥을 뜻하는 단어)'),(34,'tremble','\'tremble\'은 무서워서나 긴장해서 몸이나 목소리가 떨리는 걸 표현할 때 써.','multiple_choice',3,152,'다음 중 \'떨다, 진동하다\'의 영어 표현으로 알맞은 것은?','[\"tremble\", \"trouble\", \"terrible\", \"trigger\"]',1,'[\"tremble\"]','t로 시작하는 7글자 단어'),(35,'goosebumps','\'get goosebumps\'는 소름이 돋다, 오싹하다는 뜻의 표현이야. 무서울 때나 감동할 때 쓸 수 있어.','subjective',3,152,'\'소름이 돋다\' = get g______\n\nShe got ______ when she heard the trembling voice.\n(뜻: 떨리는 목소리를 듣고 소름이 돋았다)',NULL,2,'[\"goosebumps\", \"goose bumps\", \"Goosebumps\"]','거위 살이 돋는다고도 하지 🪿'),(36,'ran up','\'goosebumps ran up\'은 소름이 등골을 타고 올라간다는 뜻이야. crept up이나 crawled up도 같은 의미로 쓸 수 있어.','subjective',3,152,'\'등에 소름이 돋았다\'\n\nGoosebumps ______ ______ her back.\n(뜻: ~에 소름이 돋다)',NULL,3,'[\"ran up\", \"crept up\", \"crawled up\", \"went up\"]','r___ u_ (2단어, 달리기할 때도 쓰는 표현)'),(37,'confused','\'confused\'는 갑작스러운 상황에 당황하고 혼란스러운 상태를 뜻해.','multiple_choice',3,169,'다음 중 \'당황한, 혼란스러운\'의 영어 표현으로 알맞은 것은?','[\"confused\", \"confident\", \"creative\", \"curious\"]',1,'[\"confused\"]','c로 시작하는 8글자 단어'),(38,'panicked','\'panic\'은 갑작스런 상황에 당황하거나 공포에 빠지다라는 뜻이야.','subjective',3,169,'\'당황하다\' = p______\n\nShe ______ when she heard the voice.\n(뜻: 그녀는 목소리를 듣고 당황했다)',NULL,2,'[\"panicked\", \"Panicked\"]','p로 시작, 갑작스런 상황에 놀라서 어쩔 줄 모르는 상태 😰'),(39,'flustered','\'flustered\'는 예상치 못한 상황에 당황해서 어쩔 줄 모르는 상태를 표현해. bewildered, perplexed도 비슷한 의미야.','subjective',3,169,'\'당황했다\'\n\nMinhye was ______.\n(뜻: 예상하지 못한 상황에 놀라서 어쩔 줄 모르는 상태)',NULL,3,'[\"flustered\", \"bewildered\", \"perplexed\", \"confused\", \"rattled\"]','f_______d (8글자, 당황스러운 상황을 뜻하는 형용사)'),(40,'empty','\'empty\'는 아무것도 없이 텅 빈 상태를 말해. 사무실에 아무도 없는 상황이지.','multiple_choice',3,186,'다음 중 \'텅 빈, 비어있는\'을 뜻하는 영어 단어는?','[\"empty\", \"early\", \"equal\", \"exact\"]',1,'[\"empty\"]','e로 시작하는 5글자 단어'),(41,'stuck','\'be stuck to\'는 ~에 달라붙어 있다는 뜻이야. 여기서는 전화기가 귀에서 떨어지지 않는 상황을 표현해.','subjective',3,186,'\'~에 붙어 있다/달라붙다\' = s____\n\nThe phone was ______ to her ear.\n(뜻: 전화기가 그녀 귀에 붙어 있었다)',NULL,2,'[\"stuck\", \"sticking\"]','끈적끈적한 테이프처럼 달라붙는 그 단어'),(42,'obey','\'obey\'는 명령이나 의지에 따르다라는 뜻이야. 몸이 마음대로 안 될 때 자주 쓰는 표현이야.','subjective',3,186,'\'손이 말을 듣지 않았다\'\n\nMy hands wouldn\'t ______ me.\n(뜻: 내 뜻대로 움직이지 않다)',NULL,3,'[\"obey\", \"listen to\", \"respond to\", \"follow\"]','o___ (동사, 명령에 따르다)'),(43,'ring','\'ring\'은 전화벨이 울리거나 종이 울린다는 뜻이야. 새로운 희생자에게 걸려오는 무서운 전화벨 소리지.','multiple_choice',3,207,'다음 중 \'전화벨이 울리다\'를 영어로 표현할 때 알맞은 것은?','[\"ring\", \"roll\", \"rise\", \"rush\"]',1,'[\"ring\"]','r로 시작하는 4글자 동사'),(44,'ringing','\'ring\'은 전화벨이 울리다라는 뜻으로 가장 자연스럽게 쓰이는 표현이야.','subjective',3,207,'\'벨이 울리다\' = The phone is r______\n\nAt 2:47 AM, the phone started ______.\n(뜻: 새벽 2시 47분, 전화벨이 울리기 시작했다)',NULL,2,'[\"ringing\", \"Ringing\"]','r로 시작, 종이나 벨이 내는 소리를 표현할 때 써 🔔'),(45,'is ringing','\'is ringing\'이 가장 기본적인 표현이고, keeps/starts/begins ringing도 상황에 맞게 쓸 수 있어.','subjective',3,207,'이야기 마지막에서 \'전화벨이 울린다\'는 상황\n\nThe phone ______ ______.\n(현재진행형으로 표현)',NULL,3,'[\"is ringing\", \"keeps ringing\", \"starts ringing\", \"begins ringing\"]','r___ (4글자) - 벨이나 알람이 계속 울릴 때 쓰는 동사'),(46,'look around','\'look around\'는 주변을 둘러보거나 살펴본다는 뜻이야. 미란이 이상한 상황에서 주위를 확인하는 거지.','multiple_choice',4,218,'다음 중 \'주변을 둘러보다, 살펴보다\'를 뜻하는 영어 표현은?','[\"look around\", \"look after\", \"look forward\", \"look up\"]',1,'[\"look around\"]','l로 시작하는 단어, \'보다\'의 다른 표현'),(47,'around','\'look around\'는 주변을 둘러보다, 살펴보다라는 뜻의 구동사야.','subjective',4,218,'\'주변을 살펴보다\' = look a______\n\nShe looked ______ to see if anyone was there.\n(뜻: 그녀는 누군가 있는지 주변을 살펴봤다)',NULL,2,'[\"around\", \"Around\"]','a로 시작하는 \'둘러, 주위\' 의미하는 단어'),(48,'untouched','\'untouched\'는 아무도 손대지 않아서 원래 그대로인 상태를 나타내. unchanged, intact, undisturbed도 비슷한 의미로 쓸 수 있어.','subjective',4,218,'\'그대로였다\' (변화 없이 원래 상태)\n\nThe hallway was ______ as it had been after last night\'s cleaning.\n(뜻: 변화하지 않은, 손대지 않은)',NULL,3,'[\"untouched\", \"unchanged\", \"intact\", \"undisturbed\"]','un____ed (과거분사 형태, touch + ed)'),(49,'still','\'still\'은 어떤 상황이 계속되고 있을 때 \'아직, 여전히\'라는 뜻으로 써.','multiple_choice',4,229,'다음 중 \'아직, 여전히\'를 뜻하는 영어 단어는?','[\"still\", \"start\", \"study\", \"speak\"]',1,'[\"still\"]','s로 시작하는 5글자 단어'),(50,'record','\'record\'는 기록이라는 뜻으로, 보안 시스템의 출입 기록을 말할 때 자주 써.','subjective',4,229,'\'기록\' = r______\n\nThere was no ______ of anyone leaving.\n(뜻: 아무도 나간 기록이 없었다)',NULL,2,'[\"record\", \"Record\"]','r로 시작, \'녹음하다\'는 동사로도 쓰이는 그 단어 📝'),(51,'still inside','\'still inside\'는 누군가가 아직도 그 장소 안에 있다는 걸 표현할 때 쓰는 자연스러운 표현이야.','subjective',4,229,'\'아직 학교 안에 있다는 뜻이었다\'\n\nIt meant someone was ______ ______ the school.\n(뜻: 여전히 ~안에 있다)',NULL,3,'[\"still inside\", \"still in\", \"still within\", \"still at\"]','s___ i_____ (2단어, 아직도 안에)'),(52,'quiet','\'quiet\'은 소리가 없이 고요하고 조용한 상태를 말해. 아무도 없는 체육관의 고요한 분위기를 표현한 거야.','multiple_choice',4,240,'다음 중 \'고요한, 조용한\'의 영어 표현으로 알맞은 것은?','[\"quiet\", \"quick\", \"quite\", \"queen\"]',1,'[\"quiet\"]','q로 시작하는 5글자 단어'),(53,'entrance','\'entrance\'는 건물이나 장소의 입구를 뜻하는 기본 단어야.','subjective',4,240,'\'입구\' = e_______\n\nThe shoe was lying at the _______ of the gym.\n(뜻: 신발이 체육관 입구에 떨어져 있었다)',NULL,2,'[\"entrance\", \"Entrance\"]','exit의 반대말, e로 시작하는 8글자'),(54,'silent','\'silent\'는 완전히 조용한 상태를 표현해. quiet, still, hushed도 비슷한 의미로 쓸 수 있어.','subjective',4,240,'\'고요했다\'\n\nThe gymnasium was completely ______.\n(뜻: 조용한, 아무 소리가 없는)',NULL,3,'[\"silent\", \"quiet\", \"still\", \"hushed\"]','s____ (동물 조용히 하라고 할 때도 쓰는 말)'),(55,'realize','\'realize\'는 갑자기 뭔가를 깨닫거나 이해하게 될 때 쓰는 표현이야. 미란이 구두를 벗은 이유를 알아챈 상황이지.','multiple_choice',4,251,'다음 중 \'깨닫다, 알아차리다\'의 영어 표현으로 알맞은 것은?','[\"realize\", \"receive\", \"require\", \"replace\"]',1,'[\"realize\"]','r로 시작하는 7글자 단어'),(56,'obituary','\'obituary section\'은 신문의 부고란을 뜻하는 표현이야. 돌아가신 분들의 소식을 전하는 지면이지.','subjective',4,251,'\'부고란\' = o_______ section\n\nShe opened the newspaper to the ______ section.\n(뜻: 그녀는 신문의 부고란을 펼쳤다)',NULL,2,'[\"obituary\", \"Obituary\"]','o로 시작, \'bite\'나 \'chew\'의 반대말이기도 해 🦴'),(57,'obituary','\'obituary\'는 신문의 부고란이나 부고 기사를 뜻해. 사망한 사람의 생애를 기리는 글이기도 하지.','subjective',4,251,'신문의 \'부고란\'을 영어로 뭐라고 할까?\n\nShe took out a newspaper from her bag. The ______ section was open.\n(뜻: 사망 소식을 알리는 신문 란)',NULL,3,'[\"obituary\", \"obituaries\", \"death notice\", \"death notices\"]','o_______ (8글자, \'죽음\'을 뜻하는 라틴어에서 온 말)'),(58,'erase','\'erase\'는 칠판이나 글씨를 지우다라는 뜻이야. 미란이 칠판을 지운 상황이지.','multiple_choice',4,263,'다음 중 \'지우다, 없애다\'의 영어 표현으로 알맞은 것은?','[\"erase\", \"empty\", \"enter\", \"enjoy\"]',1,'[\"erase\"]','e로 시작하는 5글자 단어'),(59,'erased','\'erase the board\'는 칠판을 지우다라는 뜻이야. 이야기의 마지막 장면에서 미란이 모든 상황을 정리하며 칠판을 지우는 행동을 나타내는 표현이지.','subjective',4,263,'\'칠판을 지우다\' = e_____ the board\n\nAfter the lesson, the teacher ______ the board.\n(뜻: 수업 후 선생님이 칠판을 지웠다)',NULL,2,'[\"erased\", \"erase\"]','지우개는 eraser, 그럼 \'지우다\'는 동사는? e로 시작해'),(60,'erased','\'erase\'는 칠판이나 화이트보드의 글씨를 지울 때 쓰는 가장 자연스러운 표현이야. clean, wipe, clear도 비슷한 의미로 쓸 수 있어.','subjective',4,263,'\'칠판을 지웠다\'\n\nMiran went back to classroom 3-4 and ______ the blackboard.\n(뜻: 칠판/화이트보드의 글씨를 지우다)',NULL,3,'[\"erased\", \"cleaned\", \"wiped\", \"cleared\"]','e___ (4글자, 컴퓨터에서 파일 삭제할 때도 쓰는 단어)'),(61,'mysterious','\'mysterious\'는 뭔가 신비롭고 수수께끼 같을 때 쓰는 표현이야. 남자친구의 특별한 성격을 표현하는 거지.','multiple_choice',5,272,'다음 중 \'신비로운, 수수께끼 같은\'을 뜻하는 영어 단어는?','[\"mysterious\", \"magnificent\", \"mechanical\", \"meaningful\"]',1,'[\"mysterious\"]','m으로 시작하는 10글자 단어'),(62,'coincidence','\'coincidence\'는 우연의 일치를 뜻하는 단어야. 예측 불가능한 일이 일어났을 때 자주 쓰는 표현이지.','subjective',5,272,'\'우연\' = c________\n\nIt must have been just a ________.\n(뜻: 그냥 우연이었을 거야)',NULL,2,'[\"coincidence\", \"Coincidence\"]','동전을 던질 때 쓰는 단어와 같아 🪙'),(63,'shrugged off','\'shrug off\'는 대수롭지 않게 여기고 넘어간다는 뜻이야. brush off, pass off도 비슷한 의미로 쓸 수 있어.','subjective',5,272,'\'우연이겠지 하고 넘어갔다\'\n\nI just ______ it ______ as a coincidence.\n(뜻: ~로 여기고 넘어가다)',NULL,3,'[\"shrugged off\", \"brushed off\", \"passed off\", \"wrote off\"]','s___ o__ (2단어, 해고할 때도 쓰는 표현)'),(64,'coincidence','\'coincidence\'는 우연의 일치를 뜻해. 남자친구가 꿈을 정확히 맞춰서 우연인지 의심하는 상황이지.','multiple_choice',5,281,'다음 중 \'우연, 우연의 일치\'를 뜻하는 영어 단어는?','[\"coincidence\", \"consequence\", \"convenience\", \"conference\"]',1,'[\"coincidence\"]','c로 시작하는 11글자 단어'),(65,'coincidence','\'coincidence\'는 우연의 일치나 우연한 사건을 뜻하는 단어야.','subjective',5,281,'\'우연\' = c_________\n\nIs this just a ________?\n(뜻: 이게 그냥 우연일까?)',NULL,2,'[\"coincidence\", \"Coincidence\"]','동전을 던질 때도 쓰는 단어, c로 시작해'),(66,'tumbled down','\'tumble down the stairs\'는 계단에서 구르며 떨어지는 걸 표현해. roll down이나 fell down도 비슷한 의미로 쓸 수 있어.','subjective',5,281,'\'계단을 굴러떨어지다\'\n\nI ______ ______ the stairs in my dream.\n(뜻: 계단에서 굴러 떨어지다)',NULL,3,'[\"tumbled down\", \"rolled down\", \"fell down\", \"tumbled down\"]','t___ d___ (구르면서 떨어질 때 쓰는 표현)'),(67,'gradually','\'gradually\'는 시간이 지나면서 점점, 서서히 변화하는 걸 표현할 때 써.','multiple_choice',5,290,'다음 중 \'점점, 서서히\'를 뜻하는 영어 표현은?','[\"gradually\", \"generally\", \"gracefully\", \"gratefully\"]',1,'[\"gradually\", \"Gradually\"]','g로 시작하는 부사, \'등급\'과 같은 어원'),(68,'unconscious','\'unconscious\'는 심리학에서 무의식을 뜻하는 단어야. \'subconscious\'도 비슷한 의미로 쓰여.','subjective',5,290,'\'무의식\' = u__________\n\nFreud studied the human __________ and its influence on behavior.\n(뜻: 프로이드는 인간의 무의식과 행동에 미치는 영향을 연구했다)',NULL,2,'[\"unconscious\", \"Unconscious\", \"subconscious\", \"Subconscious\"]','un으로 시작, \'의식하지 못하는\' 마음의 영역 🧠'),(69,'connection','\'connection\'은 두 가지 사이의 연관성이나 관계를 나타내는 가장 자연스러운 표현이야. correlation, relationship 등도 비슷한 의미로 쓸 수 있어.','subjective',5,290,'\'무의식이랑 꿈의 연관성\'\n\nThe ______ between the unconscious mind and dreams\n(뜻: 두 가지 사이의 관련성, 연결)',NULL,3,'[\"connection\", \"correlation\", \"relationship\", \"link\", \"association\"]','c_______ (9글자, connect의 명사형)'),(70,'shocking','\'shocking\'은 충격적이고 놀라운 일을 발견했을 때 쓰는 표현이야.','multiple_choice',5,299,'다음 중 \'충격적인, 깜짝 놀라게 하는\'을 뜻하는 영어 단어는?','[\"shocking\", \"shaking\", \"sharing\", \"shouting\"]',1,'[\"shocking\"]','s로 시작하는 8글자, \'쇼크\'와 비슷한 단어'),(71,'manipulate','\'manipulate\'는 조작하다, 교묘히 다루다라는 뜻으로 심리적 조작에도 많이 쓰이는 단어야.','subjective',5,299,'\'조작하다, 다루다\' = m________\n\nHe was trying to _______ her dreams.\n(뜻: 그는 그녀의 꿈을 조작하려고 하고 있었다)',NULL,2,'[\"manipulate\", \"Manipulate\"]','m으로 시작, \'손으로 다루다\'에서 나온 단어 ✋'),(72,'experimenting with','\'experiment with/on\'은 누군가를 대상으로 실험하다라는 뜻이야. test with/on도 비슷한 의미로 쓸 수 있어.','subjective',5,299,'비밀리에 누군가를 실험 대상으로 삼아 연구한다\n\nHe was secretly ______ ______ her dreams.\n(뜻: ~을 대상으로 실험하다)',NULL,3,'[\"experimenting with\", \"experimenting on\", \"testing with\", \"testing on\"]','e____ w___ (2단어, 과학자들이 하는 행동)'),(73,'confused','\'confused\'는 어떻게 해야 할지 몰라서 혼란스러운 상태를 표현할 때 쓰는 단어야.','multiple_choice',5,310,'다음 중 \'혼란스러워하다, 어찌할 바를 모르다\'의 영어 표현으로 알맞은 것은?','[\"confused\", \"complete\", \"continue\", \"consider\"]',1,'[\"confused\"]','c로 시작하는 8글자 단어'),(74,'do','\'what to do\'는 \'무엇을 해야 하는지, 어떻게 해야 할지\'를 나타내는 기본적이면서도 중요한 표현이야.','subjective',5,310,'\'무엇을 해야 하는지\' = what to ___\n\nI don\'t know what to ___ about this situation.\n(뜻: 이 상황에 대해 어떻게 해야 할지 모르겠다)',NULL,2,'[\"do\", \"Do\"]','d로 시작, \'행동하다\'라는 뜻 🎭'),(75,'should do','\'What should I do?\'는 어떻게 해야 할지 모를 때 조언을 구하는 가장 자연스러운 표현이야.','subjective',5,310,'고민이 있을 때 \'어떻게 해야 할까요?\'라고 조언을 구할 때\n\nWhat ______ I ______?\n(뜻: 어떻게 해야 할까요?)',NULL,3,'[\"should do\", \"should I do\", \"am I supposed to do\", \"ought to do\"]','s_____ d_ (조언 구할 때 쓰는 정중한 표현)'),(76,'brighter','\'brighter\'는 더 밝아진, 더 활기찬 상태를 말해. 여자친구가 과외 후 달라진 모습을 표현한 거야.','multiple_choice',6,320,'다음 중 \'밝아진, 더 활기찬\'을 뜻하는 영어 표현으로 알맞은 것은?','[\"brighter\", \"bothered\", \"busiest\", \"bragged\"]',1,'[\"brighter\"]','b로 시작하는 7글자 단어, 전구가 켜진 것처럼!'),(77,'interest','\'develop an interest in\'은 어떤 것에 흥미나 관심을 갖게 되다는 뜻의 표현이야.','subjective',6,320,'\'흥미를 갖다\' = develop an i_______ in\n\nShe seems to have developed an _______ in studying.\n(뜻: 그녀는 공부에 흥미가 생긴 것 같다)',NULL,2,'[\"interest\", \"Interest\"]','관심이나 호기심을 뜻하는 i로 시작하는 명사'),(78,'irate','\'get irate\'는 짜증나거나 화가 나는 상태를 표현해. irritated, annoyed, frustrated도 같은 맥락에서 쓸 수 있어.','subjective',6,320,'\'공부 얘기만 하면 짜증을 냈다\'\n\nShe used to get ______ whenever we talked about studying.\n(뜻: 짜증나다, 화나다)',NULL,3,'[\"irate\", \"irritated\", \"annoyed\", \"frustrated\", \"upset\", \"angry\"]','i___ (4글자, irritated와 비슷한 감정)'),(79,'awkwardly','\'awkwardly\'는 어색하거나 불편한 방식으로 행동할 때 쓰는 부사야. 친구가 뭔가 숨기는 듯 어색하게 웃는 상황이지.','multiple_choice',6,330,'다음 중 \'어색하게, 불편하게\'라는 뜻의 영어 표현은?','[\"awkwardly\", \"apparently\", \"absolutely\", \"actually\"]',1,'[\"awkwardly\"]','a로 시작하는 부사, \'어색한\' 형용사에 -ly를 붙인 것'),(80,'change the subject','\'change the subject\'는 화제를 바꾸다라는 뜻의 기본적인 영어 표현이야. 불편한 주제를 피할 때 자주 쓰지.','subjective',6,330,'\'화제를 바꾸다/돌리다\' = c_______ the s______\n\nShe tried to ______ the ______ when I asked about it.\n(뜻: 내가 그것에 대해 물어보자 그녀는 화제를 돌리려 했다)',NULL,2,'[\"change the subject\", \"Change the subject\", \"changed the subject\"]','바꾸다는 \'c\', 주제는 \'s\'로 시작해. 대화 방향을 바꿀 때 쓰는 표현!'),(81,'changed the subject','\'change the subject\'는 화제를 바꾸다는 뜻이야. switch나 shift도 같은 의미로 쓸 수 있어.','subjective',6,330,'\'화제를 돌리다\'\n\nShe _______ the _______ to something else.\n(뜻: 대화 주제를 바꾸다)',NULL,3,'[\"changed the subject\", \"switched the subject\", \"shifted the subject\", \"changed subject\", \"switched subject\", \"shifted subject\"]','c_____ the s_____ (바꾸다 + 주제)'),(82,'excuse','\'excuse\'는 핑계나 변명을 뜻해. 피곤해서 잤다는 건 늦은 답장에 대한 핑계였던 거지.','multiple_choice',6,340,'다음 중 \'핑계, 변명\'을 뜻하는 영어 단어는?','[\"excuse\", \"escape\", \"expect\", \"extend\"]',1,'[\"excuse\", \"Excuse\"]','e로 시작하는 6글자 단어'),(83,'delayed','\'be delayed\'는 예정된 시간보다 늦어지거나 지연되다는 뜻이야.','subjective',6,340,'\'지연되다, 늦어지다\' = d______\n\nThe meeting will be ______ due to technical issues.\n(뜻: 회의가 기술적 문제로 지연될 예정이다)',NULL,2,'[\"delayed\", \"Delayed\"]','d로 시작, \'미루다\'라는 뜻도 있어 ⏰'),(84,'over three','\'over three hours\'는 3시간을 넘어서/초과해서라는 뜻이야. more than, past, beyond도 비슷한 의미로 쓸 수 있어.','subjective',6,340,'\'3시간이 넘어도 안 나와\'\n\nEven after ______ ______ three hours, she didn\'t come out.\n(뜻: ~이 넘다/지나다)',NULL,3,'[\"over three\", \"more than\", \"past three\", \"beyond three\"]','o___ (전치사 + 형용사, 끝나다의 over)'),(85,'excited','\'excited\'는 기분이 들뜨고 흥분된 상태를 말해. 여자친구 목소리가 평소와 달리 들떠있던 상황이지.','multiple_choice',6,350,'다음 중 \'들뜬, 흥분한\'의 영어 표현으로 알맞은 것은?','[\"excited\", \"exhausted\", \"expected\", \"extended\"]',1,'[\"excited\"]','e로 시작하는 7글자 단어'),(86,'ask','\'ask after someone\'은 누군가의 안부를 묻다라는 뜻의 구동사야.','subjective',6,350,'\'안부를 묻다\' = ask after someone\n\nI called to ______ after your daughter.\n(뜻: 따님 안부를 묻기 위해 전화했어요)',NULL,2,'[\"ask\", \"Ask\"]','a로 시작하는 단어, \'질문하다\'의 기본형'),(87,'check up','\'check up on someone\'은 누군가의 안부나 상황을 확인한다는 뜻이야. follow up, catch up도 비슷한 상황에서 쓸 수 있어.','subjective',6,350,'\'안부 인사차 전화하다\'\n\nI called her mom to ______ ______ on her.\n(뜻: 안부를 묻다, 근황을 확인하다)',NULL,3,'[\"check up\", \"follow up\", \"catch up\"]','c___ u_ (건강이나 상태를 확인할 때 쓰는 표현)'),(88,'obvious','\'obvious\'는 뭔가 분명하고 명백하다는 뜻이야. 확실하지는 않지만 뭔가 이상한 게 분명하다는 상황이지.','multiple_choice',6,360,'다음 중 \'분명한, 확실한\'을 뜻하는 영어 표현은?','[\"obvious\", \"optional\", \"opposite\", \"ordinary\"]',1,'[\"obvious\"]','o로 시작하는 7글자 단어'),(89,'definitely','\'definitely\'는 \'분명히, 확실히\'라는 뜻으로 확신을 나타낼 때 쓰는 부사야.','subjective',6,360,'\'분명한, 확실한\' = d______\n\nSomething is ______ wrong here.\n(뜻: 여기 뭔가 확실히 잘못됐어)',NULL,2,'[\"definitely\", \"Definitely\"]','d로 시작, \'명확하게\'라는 부사로도 쓰여 📍'),(90,'definitely something','\'definitely something\'은 \'분명히 뭔가\'라는 의미로, 확신을 표현할 때 쓰는 자연스러운 영어 표현이야.','subjective',6,360,'\'뭔가 이상한 게 분명하다\'\n\nBut there\'s ______ ______ something strange going on.\n(뜻: 분명히, 확실히)',NULL,3,'[\"definitely something\", \"clearly something\", \"certainly something\", \"obviously something\"]','d___ _____ (첫 번째 단어는 확실함, 두 번째는 no의 반대)');
/*!40000 ALTER TABLE `quiz_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_result`
--

DROP TABLE IF EXISTS `quiz_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content_id` bigint NOT NULL,
  `script_id` bigint NOT NULL,
  `difficulty` int NOT NULL,
  `attempt_count` int NOT NULL,
  `hint_used` bit(1) DEFAULT b'0',
  `score` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_quiz_result_content` (`content_id`),
  KEY `fk_quiz_result_script` (`script_id`),
  KEY `fk_quiz_result_user` (`user_id`),
  CONSTRAINT `fk_quiz_result_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quiz_result_script` FOREIGN KEY (`script_id`) REFERENCES `script` (`script_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quiz_result_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_result`
--

LOCK TABLES `quiz_result` WRITE;
/*!40000 ALTER TABLE `quiz_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `quiz_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `score_table`
--

DROP TABLE IF EXISTS `score_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `score_table` (
  `difficulty` int NOT NULL,
  `attempt` int NOT NULL,
  `hint_used` bit(1) NOT NULL,
  `score` int NOT NULL,
  PRIMARY KEY (`difficulty`,`attempt`,`hint_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `score_table`
--

LOCK TABLES `score_table` WRITE;
/*!40000 ALTER TABLE `score_table` DISABLE KEYS */;
INSERT INTO `score_table` VALUES (1,1,_binary '\0',20),(1,1,_binary '',16),(1,2,_binary '\0',14),(1,2,_binary '',10),(1,3,_binary '\0',8),(1,3,_binary '',4),(1,4,_binary '\0',3),(2,1,_binary '\0',20),(2,1,_binary '',17),(2,2,_binary '\0',15),(2,2,_binary '',11),(2,3,_binary '\0',9),(2,3,_binary '',5),(2,4,_binary '\0',4),(3,1,_binary '\0',20),(3,1,_binary '',18),(3,2,_binary '\0',16),(3,2,_binary '',12),(3,3,_binary '\0',10),(3,3,_binary '',6),(3,4,_binary '\0',5);
/*!40000 ALTER TABLE `score_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `script`
--

DROP TABLE IF EXISTS `script`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `script` (
  `script_id` bigint NOT NULL AUTO_INCREMENT,
  `character_name` varchar(50) DEFAULT NULL,
  `has_options` bit(1) DEFAULT NULL,
  `script_content` text,
  `sequence_num` int DEFAULT NULL,
  `content_id` bigint NOT NULL,
  `option_id` bigint DEFAULT NULL,
  `chapter_id` bigint NOT NULL,
  PRIMARY KEY (`script_id`),
  KEY `FK5ddbrpmkfuirg0d8d01yf19eq` (`option_id`),
  KEY `fk_script_content` (`content_id`),
  CONSTRAINT `FK5ddbrpmkfuirg0d8d01yf19eq` FOREIGN KEY (`option_id`) REFERENCES `script_option` (`option_id`),
  CONSTRAINT `fk_script_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`)
) ENGINE=InnoDB AUTO_INCREMENT=361 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `script`
--

LOCK TABLES `script` WRITE;
/*!40000 ALTER TABLE `script` DISABLE KEYS */;
INSERT INTO `script` VALUES (1,NULL,_binary '\0','나 대학교 다닐 때 시험기간마다 도서관 3층 64번 자리에만 앉았음.',1,1,NULL,0),(2,NULL,_binary '\0','창가 자리라서 햇빛도 잘 들고 에어컨 바람도 안 와서 딱 좋았거든.',2,1,NULL,0),(3,NULL,_binary '\0','근데 어느 날부터 그 자리에 항상 먼저 와있는 애가 있는 거임.',3,1,NULL,0),(4,NULL,_binary '\0','65번 자리에 앉아서 책 읽고 있더라고.',4,1,NULL,0),(5,NULL,_binary '\0','처음엔 짜증났음. 내 자리 옆에 왜 앉냐고.',5,1,NULL,0),(6,NULL,_binary '\0','그런데 그 애가 책상에 A4 용지 하나를 올려두고 가는 거야.',6,1,NULL,0),(7,NULL,_binary '\0','거기에 연필로 \'64번 자리 비워둘게요\' 이렇게 적어둔 거임.',7,1,NULL,0),(8,NULL,_binary '\0','뭔가 이상하다 싶었는데 일단 고맙긴 해서 그냥 앉았음.',8,1,NULL,0),(9,NULL,_binary '\0','다음 날도 똑같더라고. 65번에 그 애 있고, 64번엔 쪽지.',9,1,NULL,0),(10,NULL,_binary '\0','근데 이번엔 \'오늘 비 온대요. 우산 챙기세요\' 이렇게 적혀있음.',10,1,NULL,0),(11,NULL,_binary '\0','이게 뭐지? 싶어서 슬쩍 옆을 봤는데 그 애가 책에 집중하고 있어서 말 걸기 애매했음.',11,1,NULL,0),(12,NULL,_binary '\0','그날 정말 비 왔음. 우산 안 가져갔으면 비 맞을 뻔했어.',12,1,NULL,0),(13,NULL,_binary '\0','그 다음 날부터 호기심이 생기기 시작함.',13,1,NULL,0),(14,NULL,_binary '\0','쪽지 내용이 매일 달라지거든.',14,1,NULL,0),(15,NULL,_binary '\0','\'오늘 학식 돈까스 맛없다고 하네요\', \'3층 화장실 휴지 떨어졌어요\'',15,1,NULL,0),(16,NULL,_binary '\0','이런 식으로 은근 유용한 정보들이었음.',16,1,NULL,0),(17,NULL,_binary '\0','한 달 정도 지났을 때 그 애 얼굴을 제대로 본 적이 없다는 걸 깨달았어.',17,1,NULL,0),(18,NULL,_binary '\0','항상 내가 올 때는 이미 책 보고 있고, 내가 자리 뜰 때는 아직 앉아있고.',18,1,NULL,0),(19,NULL,_binary '\0','궁금해서 일부러 일찍 가봤는데도 이미 앉아있더라.',19,1,NULL,0),(20,NULL,_binary '\0','도대체 몇 시에 오는 거야?',20,1,NULL,0),(21,NULL,_binary '\0','어느 날 쪽지에 \'시험 잘 보세요. 항상 열심히 하시는 모습 멋있어요\' 이렇게 적혀있는 거임.',21,1,NULL,0),(22,NULL,_binary '\0','이때 뭔가 심장이 두근거렸어.',22,1,NULL,0),(23,NULL,_binary '\0','그래서 용기내서 쪽지 밑에 작은 글씨로 \'고마워요\' 이렇게 적어뒀음.',23,1,NULL,0),(24,NULL,_binary '\0','다음 날 가보니까 내 글씨 옆에 이모티콘 하나 그려져 있었어.',24,1,NULL,0),(25,NULL,_binary '\0','웃는 얼굴이었는데 되게 귀엽게 그렸더라고.',25,1,NULL,0),(26,NULL,_binary '\0','그날부터 우리 쪽지 주고받기 시작함.',26,1,NULL,0),(27,NULL,_binary '\0','근데 진짜 대화는 안 해. 계속 쪽지로만.',27,1,NULL,0),(28,NULL,_binary '\0','\'오늘 뭐 공부해요?\' \'전공이 뭐예요?\' 이런 것들.',28,1,NULL,0),(29,NULL,_binary '\0','알고 보니까 그 애는 미술과였음.',29,1,NULL,0),(30,NULL,_binary '\0','그래서 쪽지에 작은 그림도 그려주고 글씨도 예쁘게 썼던 거더라.',30,1,NULL,0),(31,NULL,_binary '\0','나는 경영학과라고 했더니 다음 날 쪽지에 작은 계산기 그림이 그려져 있었어 ㅋㅋㅋ',31,1,NULL,0),(32,NULL,_binary '\0','시험이 끝나고 방학이 되니까 도서관에 갈 일이 없어짐.',32,1,NULL,0),(33,NULL,_binary '\0','근데 그 애가 궁금한 거야.',33,1,NULL,0),(34,NULL,_binary '\0','그래서 개강하고 바로 도서관 갔는데 65번 자리가 비어있더라.',34,1,NULL,0),(35,NULL,_binary '\0','64번에 쪽지도 없고.',35,1,NULL,0),(36,NULL,_binary '\0','혹시 시간표가 바뀐 건가 싶어서 계속 다녀봤는데 한 주 내내 없었어.',36,1,NULL,0),(37,NULL,_binary '\0','그러다가 일주일 뒤에 64번 자리에 쪽지 하나가 놓여있는 거 발견함.',37,1,NULL,0),(38,NULL,_binary '\0','\'미안해요. 교환학생으로 가게 됐어요. 1년 후에 돌아와요\'',38,1,NULL,0),(39,NULL,_binary '\0','그 밑에 작은 비행기 그림하나 그려져 있었음.',39,1,NULL,0),(40,NULL,_binary '\0','진짜 허무했어. 얼굴도 제대로 못 본 채로 헤어진 거잖아.',40,1,NULL,0),(41,NULL,_binary '\0','그 뒤로 도서관 가면 65번 자리만 계속 쳐다봤어.',41,1,NULL,0),(42,NULL,_binary '\0','1년이 지나고 그 애가 돌아왔는지 확인하러 갔는데 또 없더라.',42,1,NULL,0),(43,NULL,_binary '\0','근데 며칠 뒤에 도서관 앞 게시판에 전시회 포스터 하나가 붙어있는 거 봤어.',43,1,NULL,0),(44,NULL,_binary '\0','미술과 졸업전시회였는데 포스터 한쪽 모서리에 작은 글씨로 \'64\'라고 적혀있었음.',44,1,NULL,0),(45,NULL,_binary '\0','그 글씨체가 쪽지랑 똑같았어.',45,1,NULL,0),(46,NULL,_binary '\0','전시회 가봤는데 작품 중에 도서관 그림이 하나 있더라고.',46,1,NULL,0),(47,NULL,_binary '\0','3층 창가 자리를 그린 건데 64번 책상 위에 작은 쪽지들이 쌓여있는 그림이었어.',47,1,NULL,0),(48,NULL,_binary '\0','그림 제목이 \'말 못한 인사\'였음.',48,1,NULL,0),(49,NULL,_binary '\0','그때 옆에서 누군가 말을 거는 거야. \'그 자리 아세요?\'',49,1,NULL,0),(50,NULL,_binary '\0','돌아보니까 익숙한데 낯선 얼굴이었어.',50,1,NULL,0),(51,NULL,_binary '\0','1년 동안 매일 옆에 앉았던 그 애였음.',51,1,NULL,0),(52,NULL,_binary '\0','\'1년 내내 인사하고 싶었는데 용기가 안 났어요\'라고 하더라.',52,1,NULL,0),(53,NULL,_binary '\0','나도 똑같았다고 했어.',53,1,NULL,0),(54,NULL,_binary '\0','그 애가 웃으면서 \'이제 쪽지 말고 진짜 대화할 수 있을까요?\' 이러는데 목소리가 떨리고 있었음.',54,1,NULL,0),(55,NULL,_binary '\0','나도 떨렸어.',55,1,NULL,0),(56,NULL,_binary '\0','지금도 그 애랑 만나고 있음.',56,1,NULL,0),(57,NULL,_binary '\0','아직도 가끔 쪽지로 대화해 ㅋㅋㅋ',57,1,NULL,0),(58,NULL,_binary '\0','연우는 동네 서점에서 일했다.',1,2,NULL,0),(59,NULL,_binary '\0','작은 서점이라 손님이 많지 않았다.',2,2,NULL,0),(60,NULL,_binary '\0','그런데 한 명만 매일 왔다.',3,2,NULL,0),(61,NULL,_binary '\0','학교 끝나고 오는 고등학생.',4,2,NULL,0),(62,NULL,_binary '\0','항상 같은 자리에 앉았다.',5,2,NULL,0),(63,NULL,_binary '\0','창가 쪽 3번 관람석이라고 적힌 의자.',6,2,NULL,0),(64,NULL,_binary '\0','책을 읽는 게 아니라 숙제를 했다.',7,2,NULL,0),(65,NULL,_binary '\0','수학 문제집을 펼쳐놓고 연필로 끄적거렸다.',8,2,NULL,0),(66,NULL,_binary '\0','가끔 머리를 쥐어뜯기도 했다.',9,2,NULL,0),(67,NULL,_binary '\0','연우는 그 학생을 \'3번 학생\'이라고 불렀다.',10,2,NULL,0),(68,NULL,_binary '\0','혼자서.',11,2,NULL,0),(69,NULL,_binary '\0','어느 날 그 학생이 울고 있었다.',12,2,NULL,0),(70,NULL,_binary '\0','조용히 훌쩍거리면서 문제집을 보고 있었다.',13,2,NULL,0),(71,NULL,_binary '\0','연우가 티슈를 갖다 줬다.',14,2,NULL,0),(72,NULL,_binary '\0','학생이 고맙다고 작게 말했다.',15,2,NULL,0),(73,NULL,_binary '\0','그 다음 날부터 인사를 했다.',16,2,NULL,0),(74,NULL,_binary '\0','들어올 때 고개를 살짝 숙이고 나갈 때도.',17,2,NULL,0),(75,NULL,_binary '\0','한 달쯤 지났을까.',18,2,NULL,0),(76,NULL,_binary '\0','그 학생이 안 왔다.',19,2,NULL,0),(77,NULL,_binary '\0','3번 자리가 텅 비어 있었다.',20,2,NULL,0),(78,NULL,_binary '\0','이틀째도 안 왔다.',21,2,NULL,0),(79,NULL,_binary '\0','연우는 자꾸 창가를 쳐다봤다.',22,2,NULL,0),(80,NULL,_binary '\0','일주일이 지나서야 왔다.',23,2,NULL,0),(81,NULL,_binary '\0','그런데 뭔가 달랐다.',24,2,NULL,0),(82,NULL,_binary '\0','머리를 짧게 잘랐고 교복이 아닌 검은 옷을 입고 있었다.',25,2,NULL,0),(83,NULL,_binary '\0','평소처럼 3번 자리에 앉았는데 숙제를 안 꺼냈다.',26,2,NULL,0),(84,NULL,_binary '\0','그냥 멍하니 창밖만 보고 있었다.',27,2,NULL,0),(85,NULL,_binary '\0','연우가 다가가서 물었다.',28,2,NULL,0),(86,NULL,_binary '\0','괜찮냐고.',29,2,NULL,0),(87,NULL,_binary '\0','학생이 고개를 들었다.',30,2,NULL,0),(88,NULL,_binary '\0','눈이 빨갛게 부어 있었다.',31,2,NULL,0),(89,NULL,_binary '\0','아버지가 돌아가셨다고 말했다.',32,2,NULL,0),(90,NULL,_binary '\0','갑자기 쓰러지셨다고.',33,2,NULL,0),(91,NULL,_binary '\0','연우는 뭐라고 말해야 할지 몰랐다.',34,2,NULL,0),(92,NULL,_binary '\0','그냥 옆에 앉았다.',35,2,NULL,0),(93,NULL,_binary '\0','학생이 또 말했다.',36,2,NULL,0),(94,NULL,_binary '\0','여기서 숙제하면서 아버지 생각이 많이 났다고.',37,2,NULL,0),(95,NULL,_binary '\0','아버지도 수학을 좋아하셨다고.',38,2,NULL,0),(96,NULL,_binary '\0','연우가 물었다.',39,2,NULL,0),(97,NULL,_binary '\0','그래서 수학을 하는 거냐고.',40,2,NULL,0),(98,NULL,_binary '\0','학생이 고개를 저었다.',41,2,NULL,0),(99,NULL,_binary '\0','사실 수학을 제일 싫어한다고.',42,2,NULL,0),(100,NULL,_binary '\0','그런데 아버지가 수학 선생님이셨다고.',43,2,NULL,0),(101,NULL,_binary '\0','아버지 책상에서 이 문제집을 찾았다고.',44,2,NULL,0),(102,NULL,_binary '\0','첫 페이지에 편지가 끼워져 있었다고.',45,2,NULL,0),(103,NULL,_binary '\0','학생이 가방에서 편지를 꺼냈다.',46,2,NULL,0),(104,NULL,_binary '\0','연우에게 보여줬다.',47,2,NULL,0),(105,NULL,_binary '\0','\'수학이 어려워도 포기하지 마. 아빠가 항상 응원할게. - 사랑하는 딸 지은이에게\'',48,2,NULL,0),(106,NULL,_binary '\0','편지 아래쪽에 작은 글씨로 또 적혀 있었다.',49,2,NULL,0),(107,NULL,_binary '\0','\'3번 자리에 앉으면 집중이 잘 될 거야.\'',50,2,NULL,0),(108,NULL,_binary '\0','연우가 깨달았다.',51,2,NULL,0),(109,NULL,_binary '\0','아버지가 이 서점을 알고 있었다는 걸.',52,2,NULL,0),(110,NULL,_binary '\0','지은이가 여기서 공부한다는 걸 아셨다는 걸.',53,2,NULL,0),(111,NULL,_binary '\0','지은이도 같은 생각이었나 보다.',54,2,NULL,0),(112,NULL,_binary '\0','눈물을 흘리면서 말했다.',55,2,NULL,0),(113,NULL,_binary '\0','아버지가 여기까지 찾아오셨을지도 모른다고.',56,2,NULL,0),(114,NULL,_binary '\0','딸이 어디서 공부하는지 궁금해서.',57,2,NULL,0),(115,NULL,_binary '\0','연우는 그제서야 기억했다.',58,2,NULL,0),(116,NULL,_binary '\0','한 달 전쯤 한 번 온 중년 남자를.',59,2,NULL,0),(117,NULL,_binary '\0','지은이와 닮은 얼굴이었다.',60,2,NULL,0),(118,NULL,_binary '\0','3번 자리를 유심히 보더니 아무 말 없이 나갔던.',61,2,NULL,0),(119,NULL,_binary '\0','민혜는 콜센터에서 일한다.',1,3,NULL,0),(120,NULL,_binary '\0','야간 근무를 맡은 지 3개월째.',2,3,NULL,0),(121,NULL,_binary '\0','새벽 시간대라 전화가 거의 안 온다.',3,3,NULL,0),(122,NULL,_binary '\0','대부분 술 취한 사람들의 장난 전화나 잘못 건 전화.',4,3,NULL,0),(123,NULL,_binary '\0','그런데 지난주부터 이상한 전화가 오기 시작했다.',5,3,NULL,0),(124,NULL,_binary '\0','새벽 2시 47분. 정확히 같은 시각.',6,3,NULL,0),(125,NULL,_binary '\0','전화벨이 울린다.',7,3,NULL,0),(126,NULL,_binary '\0','받으면 숨소리만 들린다.',8,3,NULL,0),(127,NULL,_binary '\0','\"안녕하세요, 고객센터입니다\" 말해도 대답이 없다.',9,3,NULL,0),(128,NULL,_binary '\0','그냥 후-후- 하는 숨소리.',10,3,NULL,0),(129,NULL,_binary '\0','처음에는 장난 전화려니 했다.',11,3,NULL,0),(130,NULL,_binary '\0','근데 매일 같은 시간이었다.',12,3,NULL,0),(131,NULL,_binary '\0','2시 47분. 정확히.',13,3,NULL,0),(132,NULL,_binary '\0','오늘도 그 시간이 다가온다.',14,3,NULL,0),(133,NULL,_binary '\0','시계를 보니 2시 46분.',15,3,NULL,0),(134,NULL,_binary '\0','민혜는 손에 땀이 났다.',16,3,NULL,0),(135,NULL,_binary '\0','왜 이렇게 긴장하고 있는 걸까?',17,3,NULL,0),(136,NULL,_binary '\0','그냥 끊어버리면 되는데.',18,3,NULL,0),(137,NULL,_binary '\0','2시 47분. 벨이 울린다.',19,3,NULL,0),(138,NULL,_binary '\0','손이 떨렸지만 받았다.',20,3,NULL,0),(139,NULL,_binary '\0','\"안녕하세요...\"',21,3,NULL,0),(140,NULL,_binary '\0','역시 숨소리만 들린다.',22,3,NULL,0),(141,NULL,_binary '\0','그런데 오늘은 뭔가 달랐다.',23,3,NULL,0),(142,NULL,_binary '\0','숨소리 뒤로 다른 소리가 섞여 있었다.',24,3,NULL,0),(143,NULL,_binary '\0','뚝뚝뚝.',25,3,NULL,0),(144,NULL,_binary '\0','물 떨어지는 소리? 아니다.',26,3,NULL,0),(145,NULL,_binary '\0','타자 치는 소리였다.',27,3,NULL,0),(146,NULL,_binary '\0','누군가 키보드를 치고 있었다.',28,3,NULL,0),(147,NULL,_binary '\0','\"거기 누구세요?\" 민혜가 물었다.',29,3,NULL,0),(148,NULL,_binary '\0','타자 소리가 멈췄다.',30,3,NULL,0),(149,NULL,_binary '\0','그리고 목소리가 들렸다.',31,3,NULL,0),(150,NULL,_binary '\0','\"도와줘.\"',32,3,NULL,0),(151,NULL,_binary '\0','여자 목소리였다. 떨리고 있었다.',33,3,NULL,0),(152,NULL,_binary '\0','민혜는 등에 소름이 돋았다.',34,3,NULL,0),(153,NULL,_binary '\0','\"어디에 계세요? 무슨 일이에요?\"',35,3,NULL,0),(154,NULL,_binary '\0','\"찾지 마. 위험해.\"',36,3,NULL,0),(155,NULL,_binary '\0','그리고 전화가 끊어졌다.',37,3,NULL,0),(156,NULL,_binary '\0','발신번호를 확인했다.',38,3,NULL,0),(157,NULL,_binary '\0','표시되지 않음.',39,3,NULL,0),(158,NULL,_binary '\0','민혜는 팀장에게 보고했다.',40,3,NULL,0),(159,NULL,_binary '\0','\"그냥 장난 전화겠지. 신경 쓰지 마.\"',41,3,NULL,0),(160,NULL,_binary '\0','하지만 신경 쓰이지 않을 수가 없었다.',42,3,NULL,0),(161,NULL,_binary '\0','그 여자의 목소리가 계속 맴돌았다.',43,3,NULL,0),(162,NULL,_binary '\0','다음 날 밤.',44,3,NULL,0),(163,NULL,_binary '\0','2시 47분. 또 전화가 왔다.',45,3,NULL,0),(164,NULL,_binary '\0','이번에는 바로 그 목소리가 들렸다.',46,3,NULL,0),(165,NULL,_binary '\0','\"왜 찾으려고 해?\"',47,3,NULL,0),(166,NULL,_binary '\0','민혜는 당황했다.',48,3,NULL,0),(167,NULL,_binary '\0','\"찾는 게 아니라... 도와주려고...\"',49,3,NULL,0),(168,NULL,_binary '\0','\"내 뒤에 있어. 지금.\"',50,3,NULL,0),(169,NULL,_binary '\0','민혜는 뒤를 돌아봤다.',51,3,NULL,0),(170,NULL,_binary '\0','아무도 없었다.',52,3,NULL,0),(171,NULL,_binary '\0','텅 빈 사무실.',53,3,NULL,0),(172,NULL,_binary '\0','\"장난하지 마세요.\"',54,3,NULL,0),(173,NULL,_binary '\0','\"장난이 아니야. 정말로 뒤에 있어.\"',55,3,NULL,0),(174,NULL,_binary '\0','전화기 너머로 또 다른 소리가 들렸다.',56,3,NULL,0),(175,NULL,_binary '\0','의자 바퀴 굴러가는 소리.',57,3,NULL,0),(176,NULL,_binary '\0','민혜는 고개를 들었다.',58,3,NULL,0),(177,NULL,_binary '\0','자신의 의자가 천천히 움직이고 있었다.',59,3,NULL,0),(178,NULL,_binary '\0','아무도 건드리지 않았는데.',60,3,NULL,0),(179,NULL,_binary '\0','전화기에서 웃음소리가 들렸다.',61,3,NULL,0),(180,NULL,_binary '\0','\"이제 알겠지?\"',62,3,NULL,0),(181,NULL,_binary '\0','민혜는 전화기를 내려놓으려 했다.',63,3,NULL,0),(182,NULL,_binary '\0','하지만 손이 말을 듣지 않았다.',64,3,NULL,0),(183,NULL,_binary '\0','전화기가 귀에 붙어 떨어지지 않았다.',65,3,NULL,0),(184,NULL,_binary '\0','목소리가 다시 들렸다.',66,3,NULL,0),(185,NULL,_binary '\0','\"3개월 전에 죽었어. 이 자리에서.\"',67,3,NULL,0),(186,NULL,_binary '\0','민혜는 기억났다.',68,3,NULL,0),(187,NULL,_binary '\0','전임자가 갑자기 그만뒀다고 들었다.',69,3,NULL,0),(188,NULL,_binary '\0','아니다. 그만둔 게 아니었다.',70,3,NULL,0),(189,NULL,_binary '\0','\"야간 근무 중에 심장마비로...\"',71,3,NULL,0),(190,NULL,_binary '\0','팀장의 말이 떠올랐다.',72,3,NULL,0),(191,NULL,_binary '\0','\"넌 내 자리에 앉아 있어.\"',73,3,NULL,0),(192,NULL,_binary '\0','민혜는 의자에서 일어나려 했다.',74,3,NULL,0),(193,NULL,_binary '\0','몸이 움직이지 않았다.',75,3,NULL,0),(194,NULL,_binary '\0','전화기 너머로 속삭임이 들렸다.',76,3,NULL,0),(195,NULL,_binary '\0','\"이제 네 차례야.\"',77,3,NULL,0),(196,NULL,_binary '\0','민혜의 심장이 빠르게 뛰기 시작했다.',78,3,NULL,0),(197,NULL,_binary '\0','숨이 점점 가빠졌다.',79,3,NULL,0),(198,NULL,_binary '\0','가슴이 아팠다.',80,3,NULL,0),(199,NULL,_binary '\0','전화기에서 마지막 목소리가 들렸다.',81,3,NULL,0),(200,NULL,_binary '\0','\"고마워. 이제 나는 자유야.\"',82,3,NULL,0),(201,NULL,_binary '\0','민혜는 의자에 고개를 떨어뜨렸다.',83,3,NULL,0),(202,NULL,_binary '\0','다음 날 아침, 팀장이 발견했다.',84,3,NULL,0),(203,NULL,_binary '\0','심장마비였다.',85,3,NULL,0),(204,NULL,_binary '\0','그리고 한 달 후.',86,3,NULL,0),(205,NULL,_binary '\0','새로운 야간 근무자가 들어왔다.',87,3,NULL,0),(206,NULL,_binary '\0','새벽 2시 47분.',88,3,NULL,0),(207,NULL,_binary '\0','전화벨이 울린다.',89,3,NULL,0),(208,NULL,_binary '\0','학교 청소부 박미란은 새벽 6시에 출근했다.',1,4,NULL,0),(209,NULL,_binary '\0','1층 복도를 걸어가다가 발견했다.',2,4,NULL,0),(210,NULL,_binary '\0','빨간 구두 한 짝.',3,4,NULL,0),(211,NULL,_binary '\0','여학생 실내화가 아니었다. 성인용 하이힐이었다.',4,4,NULL,0),(212,NULL,_binary '\0','미란은 주변을 살펴봤다.',5,4,NULL,0),(213,NULL,_binary '\0','복도는 어젯밤에 깨끗이 청소했던 그대로였다.',6,4,NULL,0),(214,NULL,_binary '\0','아무도 없는 새벽 학교에 누가?',7,4,NULL,0),(215,NULL,_binary '\0','구두는 3학년 4반 교실 문 앞에 놓여 있었다.',8,4,NULL,0),(216,NULL,_binary '\0','한 짝만. 오른쪽.',9,4,NULL,0),(217,NULL,_binary '\0','미란이 교실 문을 열었다.',10,4,NULL,0),(218,NULL,_binary '\0','칠판에 분필로 뭔가가 적혀 있었다.',11,4,NULL,0),(219,NULL,_binary '\0','\"7번 책상을 봐.\"',12,4,NULL,0),(220,NULL,_binary '\0','7번 자리는 비어 있었다. 원래 결석이 많은 학생이었다.',13,4,NULL,0),(221,NULL,_binary '\0','책상 위에 종이 한 장이 접혀 있었다.',14,4,NULL,0),(222,NULL,_binary '\0','\"체육관으로 와. 혼자.\"',15,4,NULL,0),(223,NULL,_binary '\0','미란은 휴대폰으로 경비실에 연락했다.',16,4,NULL,0),(224,NULL,_binary '\0','\"김 아저씨, 어젯밤에 학교에 누가 들어왔나요?\"',17,4,NULL,0),(225,NULL,_binary '\0','\"아뇨. 저 밤새 깨어있었는데 아무도 안 왔어요.\"',18,4,NULL,0),(226,NULL,_binary '\0','그런데 보안 기록을 확인해보니 이상한 점이 있었다.',19,4,NULL,0),(227,NULL,_binary '\0','어젯밤 11시 47분에 정문이 열렸다가 닫혔다.',20,4,NULL,0),(228,NULL,_binary '\0','하지만 다시 나간 기록은 없었다.',21,4,NULL,0),(229,NULL,_binary '\0','누군가 아직 학교 안에 있다는 뜻이었다.',22,4,NULL,0),(230,NULL,_binary '\0','체육관으로 가는 길에 미란은 또 발견했다.',23,4,NULL,0),(231,NULL,_binary '\0','왼쪽 구두.',24,4,NULL,0),(232,NULL,_binary '\0','체육관 입구 앞에 떨어져 있었다.',25,4,NULL,0),(233,NULL,_binary '\0','맨발로 들어간 거였다.',26,4,NULL,0),(234,NULL,_binary '\0','체육관 안은 고요했다.',27,4,NULL,0),(235,NULL,_binary '\0','미란이 전등을 켰다.',28,4,NULL,0),(236,NULL,_binary '\0','농구대 밑에 누군가 앉아 있었다.',29,4,NULL,0),(237,NULL,_binary '\0','20대 여성이었다. 긴 머리, 교복이 아닌 정장 차림.',30,4,NULL,0),(238,NULL,_binary '\0','맨발이었다.',31,4,NULL,0),(239,NULL,_binary '\0','\"누구세요? 여기서 뭐 하는 거예요?\"',32,4,NULL,0),(240,NULL,_binary '\0','여자가 고개를 들었다.',33,4,NULL,0),(241,NULL,_binary '\0','\"전 이 학교 졸업생이에요. 17년 전에.\"',34,4,NULL,0),(242,NULL,_binary '\0','\"밤에 왜 학교에?\"',35,4,NULL,0),(243,NULL,_binary '\0','\"3학년 4반 7번 자리에 앉던 학생이었어요.\"',36,4,NULL,0),(244,NULL,_binary '\0','미란이 깨달았다.',37,4,NULL,0),(245,NULL,_binary '\0','구두를 벗고 들어온 이유.',38,4,NULL,0),(246,NULL,_binary '\0','\"선생님한테 혼나지 않으려고 맨발로 다녔던 거군요.\"',39,4,NULL,0),(247,NULL,_binary '\0','여자가 고개를 끄덕였다.',40,4,NULL,0),(248,NULL,_binary '\0','\"그때처럼 조용히 들어오고 싶었어요.\"',41,4,NULL,0),(249,NULL,_binary '\0','\"왜 하필 지금?\"',42,4,NULL,0),(250,NULL,_binary '\0','여자가 가방에서 신문을 꺼냈다.',43,4,NULL,0),(251,NULL,_binary '\0','부고란이 펼쳐져 있었다.',44,4,NULL,0),(252,NULL,_binary '\0','\"담임 선생님이 돌아가셨대요.\"',45,4,NULL,0),(253,NULL,_binary '\0','미란은 그제야 이해했다.',46,4,NULL,0),(254,NULL,_binary '\0','\"마지막 인사를 하려고 온 거구나.\"',47,4,NULL,0),(255,NULL,_binary '\0','여자가 일어났다.',48,4,NULL,0),(256,NULL,_binary '\0','\"선생님이 항상 말씀하셨어요. 신발 소리 나지 않게 조용히 다니라고.\"',49,4,NULL,0),(257,NULL,_binary '\0','\"지금도 그 습관이 남아 있어서.\"',50,4,NULL,0),(258,NULL,_binary '\0','미란이 빨간 구두를 주워왔다.',51,4,NULL,0),(259,NULL,_binary '\0','\"이거 신고 나가세요.\"',52,4,NULL,0),(260,NULL,_binary '\0','여자가 구두를 신더니 한 번 돌아봤다.',53,4,NULL,0),(261,NULL,_binary '\0','\"고마워요. 그리고 죄송해요.\"',54,4,NULL,0),(262,NULL,_binary '\0','정문이 열리고 닫히는 소리가 들렸다.',55,4,NULL,0),(263,NULL,_binary '\0','미란은 3학년 4반으로 돌아가서 칠판을 지웠다.',56,4,NULL,0),(264,NULL,_binary '\0','저는 23살 여자고요, 남자친구랑 사귄 지 6개월 됐어요.',1,5,NULL,0),(265,NULL,_binary '\0','남자친구가 제 꿈을 미리 알아요.',2,5,NULL,0),(266,NULL,_binary '\0','처음 설명할게요. 남자친구는 예술대학에서 심리학을 복수전공하는 애예요.',3,5,NULL,0),(267,NULL,_binary '\0','성격이 되게 신비로운 타입이거든요.',4,5,NULL,0),(268,NULL,_binary '\0','첫 만남에서부터 뭔가 특이했어요.',5,5,NULL,0),(269,NULL,_binary '\0','\"오늘 밤에 파란색 나비 꿈을 꿀 것 같은데요?\" 이렇게 말하더라고요.',6,5,NULL,0),(270,NULL,_binary '\0','저는 그냥 \"아 네...ㅋㅋㅋ\" 이랬거든요.',7,5,NULL,0),(271,NULL,_binary '\0','그런데 진짜로 그날 밤에 파란 나비가 나오는 꿈을 꿨어요.',8,5,NULL,0),(272,NULL,_binary '\0','우연이겠지 하고 넘어갔는데.',9,5,NULL,0),(273,NULL,_binary '\0','두 번째 만남에서 또 말하는 거예요.',10,5,NULL,0),(274,NULL,_binary '\0','\"혹시 어릴 때 살던 집 나오는 꿈 꾸지 않았어요?\"',11,5,NULL,0),(275,NULL,_binary '\0','맞았어요. 진짜 초등학교 때 살던 빌라가 나왔어요.',12,5,NULL,0),(276,NULL,_binary '\0','세 번째 만남에서는 \"계단에서 떨어지는 꿈 조심하세요\" 이러더라고요.',13,5,NULL,0),(277,NULL,_binary '\0','그날 밤에 진짜 꿈에서 계단을 굴러떨어졌어요.',14,5,NULL,0),(278,NULL,_binary '\0','이게 우연일까요?',15,5,NULL,0),(279,NULL,_binary '\0','사귀고 나서도 계속됐어요.',16,5,NULL,0),(280,NULL,_binary '\0','매일 자기 전에 카톡이 와요.',17,5,NULL,0),(281,NULL,_binary '\0','\"오늘은 물 관련 꿈일 것 같아\" \"검은 고양이 나올 거야\" 이런 식으로.',18,5,NULL,0),(282,NULL,_binary '\0','정확도가 80% 정도 돼요.',19,5,NULL,0),(283,NULL,_binary '\0','처음엔 신기했는데 점점 무서워지기 시작했어요.',20,5,NULL,0),(284,NULL,_binary '\0','어떻게 이런 게 가능한 거예요?',21,5,NULL,0),(285,NULL,_binary '\0','남자친구한테 직접 물어봤어요.',22,5,NULL,0),(286,NULL,_binary '\0','\"야 솔직히 말해. 너 뭐야? 초능력자야?\"',23,5,NULL,0),(287,NULL,_binary '\0','그랬더니 웃으면서 \"그냥 관찰하는 거야\" 이러더라고요.',24,5,NULL,0),(288,NULL,_binary '\0','\"관찰이 뭔데?\"',25,5,NULL,0),(289,NULL,_binary '\0','\"네가 하루 종일 뭘 했는지, 뭘 먹었는지, 무슨 영화 봤는지 들으면 꿈이 예측돼\"',26,5,NULL,0),(290,NULL,_binary '\0','\"심리학으로 배운 거야. 무의식이랑 꿈의 연관성 같은 거\"',27,5,NULL,0),(291,NULL,_binary '\0','그래도 이상했어요.',28,5,NULL,0),(292,NULL,_binary '\0','그런데 한 달 전에 충격적인 걸 발견했어요.',29,5,NULL,0),(293,NULL,_binary '\0','남자친구 집에 갔는데 책상에 노트가 있었어요.',30,5,NULL,0),(294,NULL,_binary '\0','제목이 \"○○이 꿈 패턴 분석\"이었어요. 제 이름이 들어간.',31,5,NULL,0),(295,NULL,_binary '\0','펼쳐보니까 날짜별로 제 하루 일과랑 그날 밤 꿈이 정리되어 있었어요.',32,5,NULL,0),(296,NULL,_binary '\0','\"3월 5일: 매운 음식 + 스트레스 → 쫓기는 꿈 (적중)\"',33,5,NULL,0),(297,NULL,_binary '\0','\"3월 12일: 옛날 사진 봄 + 향수 → 고향 관련 꿈 (적중)\"',34,5,NULL,0),(298,NULL,_binary '\0','이런 식으로 3개월치가 빼곡히 적혀있었어요.',35,5,NULL,0),(299,NULL,_binary '\0','그리고 맨 뒤 페이지에 \"꿈 조작 실험\"이라고 써 있었어요.',36,5,NULL,0),(300,NULL,_binary '\0','뭐냐면, 제가 특정 꿈을 꿀 만한 상황을 일부러 만드는 거였어요.',37,5,NULL,0),(301,NULL,_binary '\0','데이트할 때 일부러 파란색 옷을 입고 나비 장식품을 보여준다든지.',38,5,NULL,0),(302,NULL,_binary '\0','옛날 이야기를 꺼낸다든지.',39,5,NULL,0),(303,NULL,_binary '\0','제 꿈을 예측하는 게 아니라 조종하고 있었던 거예요.',40,5,NULL,0),(304,NULL,_binary '\0','남자친구한테 따졌더니 \"미안해, 근데 네 반응이 너무 귀여워서\" 이러더라고요.',41,5,NULL,0),(305,NULL,_binary '\0','\"그리고 이거 덕분에 네 심리 상태도 더 잘 알게 되고\"',42,5,NULL,0),(306,NULL,_binary '\0','\"나쁜 의도는 아니었어\"',43,5,NULL,0),(307,NULL,_binary '\0','이 사람 저를 실험용 쥐로 본 건가요?',44,5,NULL,0),(308,NULL,_binary '\0','근데 또 나쁜 사람은 아닌 것 같아요.',45,5,NULL,0),(309,NULL,_binary '\0','제가 스트레스받을 때 미리 알고 챙겨주거든요.',46,5,NULL,0),(310,NULL,_binary '\0','어떻게 해야 할까요...?',47,5,NULL,0),(311,NULL,_binary '\0','고3 여자친구랑 8개월 사귀고 있음.',1,6,NULL,0),(312,NULL,_binary '\0','내가 대학교 1학년이고 걔가 고등학교 3학년인데 동네 선후배 사이였음.',2,6,NULL,0),(313,NULL,_binary '\0','원래 공부 잘하는 애가 아니라서 부모님이 과외 선생님을 붙여줬다더라.',3,6,NULL,0),(314,NULL,_binary '\0','수학 과외인데 대학생 형이 온다고 함.',4,6,NULL,0),(315,NULL,_binary '\0','처음에는 그냥 좋았음. 성적이 올라가면 나도 뿌듯하니까.',5,6,NULL,0),(316,NULL,_binary '\0','근데 몇 주 지나더니 과외 얘기를 자주 하기 시작함.',6,6,NULL,0),(317,NULL,_binary '\0','\"오빠가 문제 이렇게 설명해줘서 이해했어\" 이런 식으로.',7,6,NULL,0),(318,NULL,_binary '\0','뭔가 밝아진 느낌? 원래는 공부 얘기만 하면 짜증내던 애가.',8,6,NULL,0),(319,NULL,_binary '\0','그래도 처음에는 공부에 흥미가 생긴 건가 싶었음.',9,6,NULL,0),(320,NULL,_binary '\0','근데 하루는 데이트하는데 문자가 와서 \"잠깐만\" 하면서 답장하는 거임.',10,6,NULL,0),(321,NULL,_binary '\0','누구냐고 물어봤더니 \"과외 선생님이 숙제 관련해서 물어봤어\" 이러더라.',11,6,NULL,0),(322,NULL,_binary '\0','주말에도 과외와 관련된 문자를 주고받나?',12,6,NULL,0),(323,NULL,_binary '\0','이상했지만 그냥 넘어갔음. 공부 열심히 하는 건 좋은 일이니까.',13,6,NULL,0),(324,NULL,_binary '\0','그런데 이번 주에 걔 친구를 만났는데.',14,6,NULL,0),(325,NULL,_binary '\0','\"요즘 과외 어때?\" 물어봤더니 친구가 뭔가 어색하게 웃더라.',15,6,NULL,0),(326,NULL,_binary '\0','\"그냥... 열심히 하는 것 같아\" 이러는데 말투가 이상함.',16,6,NULL,0),(327,NULL,_binary '\0','그래서 \"과외 선생님은 어떤 분이야?\" 물어봤더니.',17,6,NULL,0),(328,NULL,_binary '\0','친구가 잠깐 멈추더니 \"잘 모르겠어, 직접 물어봐\" 이러고 화제를 돌리더라고.',18,6,NULL,0),(329,NULL,_binary '\0','그날부터 신경이 쓰이기 시작함.',19,6,NULL,0),(330,NULL,_binary '\0','과외 하는 날을 유심히 보니까 평소보다 화장을 진하게 함.',20,6,NULL,0),(331,NULL,_binary '\0','원래는 민낯으로 다니는 애였는데.',21,6,NULL,0),(332,NULL,_binary '\0','그리고 과외 끝나고 연락하면 답장이 늦어짐.',22,6,NULL,0),(333,NULL,_binary '\0','\"피곤해서 잠깐 잤어\" 이런 핑계를 대는데.',23,6,NULL,0),(334,NULL,_binary '\0','어느 날 과외 하는 날에 집 앞에 가봤음.',24,6,NULL,0),(335,NULL,_binary '\0','걔 집 근처 카페에서 기다리면서 언제 나오나 봤거든.',25,6,NULL,0),(336,NULL,_binary '\0','과외는 보통 2시간 한다고 했는데 3시간이 넘어도 안 나와.',26,6,NULL,0),(337,NULL,_binary '\0','그래서 전화했더니 \"아 지금 좀 어려운 문제 풀고 있어서 조금 늦어질 것 같아\" 이러더라.',27,6,NULL,0),(338,NULL,_binary '\0','전화 끊고 30분 더 기다리니까 드디어 나옴.',28,6,NULL,0),(339,NULL,_binary '\0','그런데 과외 선생님도 같이 나오는 거임.',29,6,NULL,0),(340,NULL,_binary '\0','멀어서 정확히는 안 보이는데 둘이 뭔가 얘기하면서 웃고 있더라.',30,6,NULL,0),(341,NULL,_binary '\0','그리고 헤어지기 전에 잠깐 손을 마주쳤음. 악수인지 뭔지는 모르겠지만.',31,6,NULL,0),(342,NULL,_binary '\0','집에 와서 \"과외 어땠어?\" 물어봤더니 \"그냥 평소대로\" 이러는 거임.',32,6,NULL,0),(343,NULL,_binary '\0','\"오늘 좀 늦게 끝났네?\" 했더니 \"응, 모르는 게 많아서\" 이러더라.',33,6,NULL,0),(344,NULL,_binary '\0','그런데 목소리가 뭔가 들떠 있었음.',34,6,NULL,0),(345,NULL,_binary '\0','며칠 뒤에 걔 엄마한테 안부 인사차 전화했음.',35,6,NULL,0),(346,NULL,_binary '\0','\"과외 효과 있나요?\" 물어봤더니 뭔가 이상한 말씀을 하시더라.',36,6,NULL,0),(347,NULL,_binary '\0','\"요즘 공부보다는 다른 걸 배우는 것 같아요\" 이러시는 거임.',37,6,NULL,0),(348,NULL,_binary '\0','무슨 뜻이냐고 했더니 \"글쎄요, 성적은 그대로인데 기분이 좋아 보여요\" 이러시더라.',38,6,NULL,0),(349,NULL,_binary '\0','그래서 어제 과외 선생님 번호를 알아냈음.',39,6,NULL,0),(350,NULL,_binary '\0','걔한테 \"과외 선생님 연락처 좀 달라, 나도 과외받고 싶어\" 이렇게 말해서.',40,6,NULL,0),(351,NULL,_binary '\0','의심 안 하고 바로 줬는데.',41,6,NULL,0),(352,NULL,_binary '\0','전화해서 \"안녕하세요, 과외 받고 있는 학생 남자친구입니다\" 이렇게 시작했음.',42,6,NULL,0),(353,NULL,_binary '\0','그랬더니 목소리가 확 달라지더라. 뭔가 당황하는 느낌?',43,6,NULL,0),(354,NULL,_binary '\0','\"아... 네, 안녕하세요\" 이러는데 어색함.',44,6,NULL,0),(355,NULL,_binary '\0','\"혹시 시간 되실 때 한 번 뵙고 과외 방식에 대해 상담받을 수 있을까요?\" 했더니.',45,6,NULL,0),(356,NULL,_binary '\0','\"그게... 요즘 좀 바빠서... 나중에 연락드릴게요\" 이러고 전화를 끊어버림.',46,6,NULL,0),(357,NULL,_binary '\0','보통 과외 선생님이면 학부모나 지인이 연락하면 반가워해야 하는 거 아님?',47,6,NULL,0),(358,NULL,_binary '\0','지금 여자친구한테 말해야 할지 그냥 넘어가야 할지 모르겠음.',48,6,NULL,0),(359,NULL,_binary '\0','확실한 건 아무것도 없는데 괜히 의심하는 건가 싶기도 하고.',49,6,NULL,0),(360,NULL,_binary '\0','근데 뭔가 이상한 건 분명함.',50,6,NULL,0);
/*!40000 ALTER TABLE `script` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `script_option`
--

DROP TABLE IF EXISTS `script_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `script_option` (
  `option_id` bigint NOT NULL AUTO_INCREMENT,
  `ending_score` int DEFAULT NULL,
  `option_text` varchar(500) DEFAULT NULL,
  `next_script_id` bigint DEFAULT NULL,
  `script_id` bigint NOT NULL,
  PRIMARY KEY (`option_id`),
  KEY `FKgepisfstw30ttchctvjykl1fk` (`next_script_id`),
  KEY `FKaysdv5ien4y0plo0qmkdwcj41` (`script_id`),
  CONSTRAINT `FKaysdv5ien4y0plo0qmkdwcj41` FOREIGN KEY (`script_id`) REFERENCES `script` (`script_id`),
  CONSTRAINT `FKgepisfstw30ttchctvjykl1fk` FOREIGN KEY (`next_script_id`) REFERENCES `script` (`script_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `script_option`
--

LOCK TABLES `script_option` WRITE;
/*!40000 ALTER TABLE `script_option` DISABLE KEYS */;
/*!40000 ALTER TABLE `script_option` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_asset`
--

DROP TABLE IF EXISTS `user_asset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_asset` (
  `asset_id` bigint NOT NULL AUTO_INCREMENT,
  `current_points` int DEFAULT NULL,
  `streak_days` int DEFAULT NULL,
  `total_exp` int DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`asset_id`),
  UNIQUE KEY `UKhb7y0msn68vxffcsprm86y9l5` (`user_id`),
  CONSTRAINT `FK2swjam788mhmfjd6e3iiv1xg8` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_asset`
--

LOCK TABLES `user_asset` WRITE;
/*!40000 ALTER TABLE `user_asset` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_asset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_progress`
--

DROP TABLE IF EXISTS `user_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_progress` (
  `progress_id` bigint NOT NULL AUTO_INCREMENT,
  `progress_rate` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content_id` bigint NOT NULL,
  `last_content_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `is_attained` bit(1) DEFAULT b'0',
  `achieve_date` date DEFAULT NULL,
  `completed_content_id` bigint DEFAULT NULL,
  `used_hints_count` int DEFAULT '0',
  `last_chapter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`progress_id`),
  KEY `FKgy8w0vyjc0chyh1k3fqvm58mb` (`content_id`),
  KEY `FKrt37sneeps21829cuqetjm5ye` (`user_id`),
  KEY `fk_progress_last_content` (`last_content_id`),
  KEY `FKdpmojrcfahqsg4eur8o7c9ydl` (`last_chapter_id`),
  CONSTRAINT `fk_progress_last_content` FOREIGN KEY (`last_content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `FKdpmojrcfahqsg4eur8o7c9ydl` FOREIGN KEY (`last_chapter_id`) REFERENCES `chapter` (`chapter_id`),
  CONSTRAINT `FKgy8w0vyjc0chyh1k3fqvm58mb` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `FKrt37sneeps21829cuqetjm5ye` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_progress`
--

LOCK TABLES `user_progress` WRITE;
/*!40000 ALTER TABLE `user_progress` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_study_log`
--

DROP TABLE IF EXISTS `user_study_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_study_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_correct` bit(1) DEFAULT NULL,
  `user_response` text,
  `quiz_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`log_id`),
  KEY `FKrf8vxohbg12yki1e4r46jyymo` (`quiz_id`),
  KEY `FKkusbp9o9unoh1cva7gohhanje` (`user_id`),
  CONSTRAINT `FKkusbp9o9unoh1cva7gohhanje` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKrf8vxohbg12yki1e4r46jyymo` FOREIGN KEY (`quiz_id`) REFERENCES `quiz_detail` (`quiz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_study_log`
--

LOCK TABLES `user_study_log` WRITE;
/*!40000 ALTER TABLE `user_study_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_study_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `auth_provider` varchar(20) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `email_verified` bit(1) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `social_id` varchar(100) DEFAULT NULL,
  `continuous_streak_days` int DEFAULT '0',
  `create_at` datetime(6) DEFAULT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `story_nickname` varchar(50) DEFAULT NULL,
  `language` varchar(10) DEFAULT NULL,
  `level_code` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-31 16:17:27
