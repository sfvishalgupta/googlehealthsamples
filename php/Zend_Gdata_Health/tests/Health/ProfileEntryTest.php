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

require_once 'Zend/Gdata/Health.php';
require_once 'Zend/Gdata/Health/ProfileEntry.php';

/**
 * @package Zend_Gdata
 * @subpackage UnitTests
 */
class Zend_Gdata_Health_ProfileEntryTest extends PHPUnit_Framework_TestCase
{

    public function setUp()
    {
        $this->entry = new Zend_Gdata_Health_ProfileEntry();
        $this->entryText = file_get_contents(
            'Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample.xml', 
            true);
    }
    
    public function testEmptyProfileEntry()
    { 
        $this->assertTrue(is_array($this->entry->extensionElements));
        $this->assertTrue(count($this->entry->extensionElements) == 0);
        $this->assertTrue($this->entry->getCCR() === null);
    }
    
    public function testEmptyProfileEntryToAndFromStringShouldMatch() {
        $this->entry->transferFromXML($this->entryText);
        $entryXml = $this->entry->saveXML();
        $newProfileEntry = new Zend_Gdata_Health_ProfileEntry();
        $newProfileEntry->transferFromXML($entryXml);
        $newProfileEntryXML = $newProfileEntry->saveXML();
        $this->assertTrue($entryXml == $newProfileEntryXML);
    }
        
    public function testGetAllCCRFromProfileEntry()
    {
        $this->entry->transferFromXML($this->entryText);
        $ccr = $this->entry->getCCR();
        $this->assertTrue($ccr instanceof Zend_Gdata_Health_Extension_Ccr);
        $this->assertXmlStringEqualsXmlString(file_get_contents(
            'Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample_just_ccr.xml', true), $ccr->getXML());
    }
    
    public function testSetCCRInProfileEntry()
    {
        $this->entry->transferFromXML($this->entryText);
        $ccrXML = file_get_contents(
            'Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample_just_ccr.xml', true);
        $ccrElement = $this->entry->setCCR($ccrXML);
        $this->assertTrue($ccrElement instanceof Zend_Gdata_Health_Extension_Ccr);
        $this->assertXmlStringEqualsXmlString(file_get_contents(
            'Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample_just_ccr.xml', true), $this->entry->getCCR()->getXML());
    }
    
    /*  
     *  These functions test the magic _call method within Zend_Gdata_Health_Extension_Ccr
     */    
    public function testGetCCRMedicationsFromProfileEntry()
    {
        $this->entry->transferFromXML($this->entryText);
        $medications = $this->entry->getCCR()->getMedications();
        $this->assertEquals(count($medications), 3);
        
        foreach ($medications as $index => $med) {
            $this->assertXmlStringEqualsXmlString(file_get_contents(
                "Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample_medications{$index}.xml", true), 
                $med->getXML());
        }
    }
    
    public function testGetCCRConditionsFromProfileEntry()
    {
        $this->entry->transferFromXML($this->entryText);
        $problems = $this->entry->getCCR()->getProblems();
        $conditions = $this->entry->getCCR()->getConditions();
        $this->assertSame($problems, $conditions);
        
        $this->assertEquals(count($conditions), 2);
        foreach ($conditions as $index => $condition) {
            $this->assertXmlStringEqualsXmlString(file_get_contents(
                "Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample_condition{$index}.xml", true), 
                $condition->getXML());
        }
    }
    
    public function testGetCCRAllerigiesFromProfileEntry()
    {
        $this->entry->transferFromXML($this->entryText);
        $allergies = $this->entry->getCCR()->getAllergies();
        $alerts = $this->entry->getCCR()->getAlerts();
        $this->assertSame($allergies, $alerts);
        
        $this->assertEquals(count($alerts), 2);
        foreach ($alerts as $index => $alert) {
            $this->assertXmlStringEqualsXmlString(file_get_contents(
                "Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample_allergy{$index}.xml", true), 
                $alert->getXML());
        }
    }
    
    public function testGetCCRLabResultsFromProfileEntry()
    {
        $this->entry->transferFromXML($this->entryText);
        $labresults = $this->entry->getCCR()->getLabResults();
        $results = $this->entry->getCCR()->getResults();
        $this->assertSame($labresults, $results);
        
        $this->assertEquals(count($results), 1);
        foreach ($results as $index => $result) {
            $this->assertXmlStringEqualsXmlString(file_get_contents(
                "Zend/Gdata/Health/_files/TestDataHealthProfileEntrySample_results{$index}.xml", true), 
                $result->getXML());
        }
    }
}
