<?php

/**
 * Zend Framework
 *
 * LICENSE
 *
 * This source file is subject to the new BSD license that is bundled
 * with this package in the file LICENSE.txt.
 * It is also available through the world-wide-web at this URL:
 * http://framework.zend.com/license/new-bsd
 * If you did not receive a copy of the license and are unable to
 * obtain it through the world-wide-web, please send an email
 * to license@zend.com so we can send you a copy immediately.
 *
 * @category   Zend
 * @package    Zend_Gdata
 * @subpackage UnitTests
 * @copyright  Copyright (c) 2006 Zend Technologies USA Inc. (http://www.zend.com)
 * @license    http://framework.zend.com/license/new-bsd     New BSD License
 */

/**
 * Test helper
 */
require_once dirname(dirname(dirname(__FILE__))) . DIRECTORY_SEPARATOR . 'TestHelper.php';

if (!defined('PHPUnit_MAIN_METHOD')) {
    define('PHPUnit_MAIN_METHOD', 'Zend_Gdata_AllTests::main');
}

require_once 'PHPUnit/Framework/TestSuite.php';
require_once 'PHPUnit/TextUI/TestRunner.php';

/**
 * Tests of the authentication URL generator
 */
require_once 'Zend/Gdata/AuthSubTest.php';

/**
 * Tests that do not require online access to servers
 */
require_once 'Zend/Gdata/AppTest.php';
require_once 'Zend/Gdata/App/UtilTest.php';
require_once 'Zend/Gdata/App/AuthorTest.php';
require_once 'Zend/Gdata/App/CategoryTest.php';
require_once 'Zend/Gdata/App/ContentTest.php';
require_once 'Zend/Gdata/App/ControlTest.php';
require_once 'Zend/Gdata/App/FeedTest.php';
require_once 'Zend/Gdata/App/GeneratorTest.php';
require_once 'Zend/Gdata/App/CaptchaRequiredExceptionTest.php';
require_once 'Zend/Gdata/GdataTest.php';
require_once 'Zend/Gdata/QueryTest.php';

require_once 'Zend/Gdata/AttendeeStatusTest.php';
require_once 'Zend/Gdata/AttendeeTypeTest.php';
require_once 'Zend/Gdata/CommentsTest.php';
require_once 'Zend/Gdata/EntryLinkTest.php';
require_once 'Zend/Gdata/EventStatusTest.php';
require_once 'Zend/Gdata/ExtendedPropertyTest.php';
require_once 'Zend/Gdata/FeedLinkTest.php';
require_once 'Zend/Gdata/OpenSearchItemsPerPageTest.php';
require_once 'Zend/Gdata/OpenSearchStartIndexTest.php';
require_once 'Zend/Gdata/OpenSearchTotalResultsTest.php';
require_once 'Zend/Gdata/OriginalEventTest.php';
require_once 'Zend/Gdata/RecurrenceTest.php';
require_once 'Zend/Gdata/RecurrenceExceptionTest.php';
require_once 'Zend/Gdata/ReminderTest.php';
require_once 'Zend/Gdata/TransparencyTest.php';
require_once 'Zend/Gdata/VisibilityTest.php';
require_once 'Zend/Gdata/WhenTest.php';
require_once 'Zend/Gdata/WhereTest.php';
require_once 'Zend/Gdata/WhoTest.php';

require_once 'Zend/Gdata/Gbase/ItemEntryTest.php';
require_once 'Zend/Gdata/Gbase/ItemFeedTest.php';        
require_once 'Zend/Gdata/Gbase/ItemQueryTest.php';
require_once 'Zend/Gdata/Gbase/SnippetFeedTest.php';
require_once 'Zend/Gdata/Gbase/SnippetQueryTest.php';        
require_once 'Zend/Gdata/Gbase/QueryTest.php';
require_once 'Zend/Gdata/Gbase/BaseAttributeTest.php';

require_once 'Zend/Gdata/CalendarTest.php';
require_once 'Zend/Gdata/CalendarFeedTest.php';
require_once 'Zend/Gdata/CalendarEventTest.php';
require_once 'Zend/Gdata/CalendarFeedCompositeTest.php';
require_once 'Zend/Gdata/Calendar/EventQueryTest.php';
require_once 'Zend/Gdata/Calendar/EventQueryExceptionTest.php';
require_once 'Zend/Gdata/Calendar/EventEntryTest.php';
require_once 'Zend/Gdata/Calendar/AccessLevelTest.php';
require_once 'Zend/Gdata/Calendar/ColorTest.php';
require_once 'Zend/Gdata/Calendar/HiddenTest.php';
require_once 'Zend/Gdata/Calendar/LinkTest.php';
require_once 'Zend/Gdata/Calendar/SelectedTest.php';
require_once 'Zend/Gdata/Calendar/SendEventNotificationsTest.php';
require_once 'Zend/Gdata/Calendar/TimezoneTest.php';
require_once 'Zend/Gdata/Calendar/WebContentTest.php';
require_once 'Zend/Gdata/Calendar/QuickAddTest.php';

require_once 'Zend/Gdata/Spreadsheets/ColCountTest.php';
require_once 'Zend/Gdata/Spreadsheets/RowCountTest.php';
require_once 'Zend/Gdata/Spreadsheets/CellTest.php';
require_once 'Zend/Gdata/Spreadsheets/CustomTest.php';
require_once 'Zend/Gdata/Spreadsheets/WorksheetEntryTest.php';
require_once 'Zend/Gdata/Spreadsheets/CellEntryTest.php';
require_once 'Zend/Gdata/Spreadsheets/ListEntryTest.php';
require_once 'Zend/Gdata/Spreadsheets/SpreadsheetFeedTest.php';
require_once 'Zend/Gdata/Spreadsheets/WorksheetFeedTest.php';
require_once 'Zend/Gdata/Spreadsheets/CellFeedTest.php';
require_once 'Zend/Gdata/Spreadsheets/ListFeedTest.php';
require_once 'Zend/Gdata/Spreadsheets/DocumentQueryTest.php';
require_once 'Zend/Gdata/Spreadsheets/CellQueryTest.php';
require_once 'Zend/Gdata/Spreadsheets/ListQueryTest.php';

require_once 'Zend/Gdata/Docs/DocumentListFeedTest.php';
require_once 'Zend/Gdata/Docs/DocumentListEntryTest.php';
require_once 'Zend/Gdata/Docs/QueryTest.php';

require_once 'Zend/Gdata/Photos/PhotosAlbumEntryTest.php';
require_once 'Zend/Gdata/Photos/PhotosAlbumFeedTest.php';
require_once 'Zend/Gdata/Photos/PhotosAlbumQueryTest.php';
require_once 'Zend/Gdata/Photos/PhotosCommentEntryTest.php';
require_once 'Zend/Gdata/Photos/PhotosPhotoEntryTest.php';
require_once 'Zend/Gdata/Photos/PhotosPhotoFeedTest.php';
require_once 'Zend/Gdata/Photos/PhotosPhotoQueryTest.php';
require_once 'Zend/Gdata/Photos/PhotosTagEntryTest.php';
require_once 'Zend/Gdata/Photos/PhotosUserEntryTest.php';
require_once 'Zend/Gdata/Photos/PhotosUserFeedTest.php';
require_once 'Zend/Gdata/Photos/PhotosUserQueryTest.php';

require_once 'Zend/Gdata/GappsTest.php';
require_once 'Zend/Gdata/Gapps/EmailListEntryTest.php';
require_once 'Zend/Gdata/Gapps/EmailListFeedTest.php';
require_once 'Zend/Gdata/Gapps/EmailListQueryTest.php';
require_once 'Zend/Gdata/Gapps/EmailListRecipientEntryTest.php';
require_once 'Zend/Gdata/Gapps/EmailListRecipientFeedTest.php';
require_once 'Zend/Gdata/Gapps/EmailListRecipientQueryTest.php';
require_once 'Zend/Gdata/Gapps/EmailListTest.php';
require_once 'Zend/Gdata/Gapps/ErrorTest.php';
require_once 'Zend/Gdata/Gapps/LoginTest.php';
require_once 'Zend/Gdata/Gapps/NameTest.php';
require_once 'Zend/Gdata/Gapps/NicknameEntryTest.php';
require_once 'Zend/Gdata/Gapps/NicknameFeedTest.php';
require_once 'Zend/Gdata/Gapps/NicknameQueryTest.php';
require_once 'Zend/Gdata/Gapps/NicknameTest.php';
require_once 'Zend/Gdata/Gapps/QuotaTest.php';
require_once 'Zend/Gdata/Gapps/ServiceExceptionTest.php';
require_once 'Zend/Gdata/Gapps/UserEntryTest.php';
require_once 'Zend/Gdata/Gapps/UserFeedTest.php';
require_once 'Zend/Gdata/Gapps/UserQueryTest.php';

require_once 'Zend/Gdata/YouTube/PlaylistListFeedTest.php';
require_once 'Zend/Gdata/YouTube/PlaylistListEntryTest.php';
require_once 'Zend/Gdata/YouTube/SubscriptionFeedTest.php';
require_once 'Zend/Gdata/YouTube/SubscriptionEntryTest.php';
require_once 'Zend/Gdata/YouTube/PlaylistVideoEntryTest.php';
require_once 'Zend/Gdata/YouTube/VideoEntryTest.php';
require_once 'Zend/Gdata/YouTube/PlaylistVideoFeedTest.php';
require_once 'Zend/Gdata/YouTube/VideoFeedTest.php';
require_once 'Zend/Gdata/YouTube/UserProfileEntryTest.php';
require_once 'Zend/Gdata/YouTube/CommentFeedTest.php';
require_once 'Zend/Gdata/YouTube/CommentEntryTest.php';
require_once 'Zend/Gdata/YouTube/ContactFeedTest.php';
require_once 'Zend/Gdata/YouTube/ContactEntryTest.php';

require_once 'Zend/Gdata/Health/QueryTest.php';
require_once 'Zend/Gdata/Health/ProfileListEntryTest.php';
require_once 'Zend/Gdata/Health/ProfileEntryTest.php';
require_once 'Zend/Gdata/Health/ProfileFeedTest.php';


/**
 * Tests that do require online access to servers
 * and authentication credentials
 */
require_once 'Zend/Gdata/GdataOnlineTest.php';
require_once 'Zend/Gdata/GbaseOnlineTest.php';
require_once 'Zend/Gdata/CalendarOnlineTest.php';
require_once 'Zend/Gdata/HealthOnlineTest.php';
require_once 'Zend/Gdata/SpreadsheetsOnlineTest.php';
require_once 'Zend/Gdata/DocsOnlineTest.php';
require_once 'Zend/Gdata/PhotosOnlineTest.php';
require_once 'Zend/Gdata/GappsOnlineTest.php';
require_once 'Zend/Gdata/YouTubeOnlineTest.php';
require_once 'Zend/Gdata/SkipTests.php';

class Zend_Gdata_AllTests
{
    public static function main()
    {        
        PHPUnit_TextUI_TestRunner::run(self::suite()); 
    }

    public static function suite()
    {
        $suite = new PHPUnit_Framework_TestSuite('Zend Framework - Zend_Gdata');

        $suite->addTestSuite('Zend_Gdata_Health_QueryTest');
        $suite->addTestSuite('Zend_Gdata_Health_ProfileListEntryTest');
        $suite->addTestSuite('Zend_Gdata_Health_ProfileEntryTest');
        $suite->addTestSuite('Zend_Gdata_Health_ProfileFeedTest');


        $skippingOnlineTests = true;
        if (defined('TESTS_ZEND_GDATA_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_ONLINE_ENABLED') == true &&
            defined('TESTS_ZEND_GDATA_CLIENTLOGIN_ENABLED') &&
            constant('TESTS_ZEND_GDATA_CLIENTLOGIN_ENABLED') == true) {
            /**
             * Tests that do require online access to servers
             * and authentication credentials
             */
            $skippingOnlineTests = false;
            if (defined('TESTS_ZEND_GDATA_BLOGGER_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_BLOGGER_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_GdataOnlineTest');
            }

            if (defined('TESTS_ZEND_GDATA_GBASE_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_GBASE_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_GbaseOnlineTest');
            }

            if (defined('TESTS_ZEND_GDATA_CALENDAR_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_CALENDAR_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_CalendarOnlineTest');
            }
            
            if (defined('TESTS_ZEND_GDATA_SPREADSHEETS_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_SPREADSHEETS_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_SpreadsheetsOnlineTest');
            }

            if (defined('TESTS_ZEND_GDATA_DOCS_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_DOCS_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_DocsOnlineTest');
            }

            if (defined('TESTS_ZEND_GDATA_PHOTOS_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_PHOTOS_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_PhotosOnlineTest');
            }
            
            if (defined('TESTS_ZEND_GDATA_HEALTH_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_HEALTH_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_HealthOnlineTest');
            }
        } 
        if (defined('TESTS_ZEND_GDATA_ONLINE_ENABLED') &&
                   constant('TESTS_ZEND_GDATA_ONLINE_ENABLED') == true) {
            /**
             * Tests that do require online access to servers, but
             * don't require the standard authentication credentials
             */ 
            $skippingOnlineTests = false;
            if (defined('TESTS_ZEND_GDATA_GAPPS_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_GAPPS_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_GappsOnlineTest');
            }
            if (defined('TESTS_ZEND_GDATA_YOUTUBE_ONLINE_ENABLED') &&
            constant('TESTS_ZEND_GDATA_YOUTUBE_ONLINE_ENABLED') == true) {
                $suite->addTestSuite('Zend_Gdata_YouTubeOnlineTest');
            }
        }
        if ($skippingOnlineTests) {
            $suite->addTestSuite('Zend_Gdata_SkipOnlineTest');
        }
        return $suite;
    }

}

if (PHPUnit_MAIN_METHOD == 'Zend_Gdata_AllTests::main') {
    Zend_Gdata_AllTests::main();
}
