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
 * @copyright  Copyright (c) 2005-2008 Zend Technologies USA Inc. (http://www.zend.com)
 * @license    http://framework.zend.com/license/new-bsd     New BSD License
 * @version    $Id: Entry.php 3941 2007-03-14 21:36:13Z darby $
 */

/**
 * @see Zend_Gdata_App_Extension_Element
 */
require_once 'Zend/Gdata/App/Extension/Element.php';

/**
 * Concrete class for working with CCR elements.
 *
 * @category   Zend
 * @package    Zend_Gdata
 * @copyright  Copyright (c) 2005-2008 Zend Technologies USA Inc. (http://www.zend.com)
 * @license    http://framework.zend.com/license/new-bsd     New BSD License
 */
class Zend_Gdata_Health_Extension_Ccr extends Zend_Gdata_App_Extension_Element
{
    protected $_rootNamespace = 'ccr';
    protected $_rootElement = 'ContinuityOfCareRecord';

    /**
     * Creates a Zend_Gdata_Health_Extension_Ccr entry, representing CCR data
     *
     * @param DOMElement $element (optional) DOMElement from which this
     *          object should be constructed.
     */
    public function __construct($element = null)
    {
        foreach (Zend_Gdata_Health::$namespaces as $nsPrefix => $nsUri) {
            $this->registerNamespace($nsPrefix, $nsUri);
        }
    }

    /**
     * Magic helper that allows drilling down and returning specific elements 
     * in the CCR. For example, to retrieve the users medications
     * (/ContinuityOfCareRecord/Body/Medications) from the entry's CCR, call
     * $entry->getCCR()->getMedications().  Similarly, getConditions() would
     * return extract the user's conditions.
     *
     * @param string $name Name of the function to call
     * @return array.<DOMElement> A list of the appropriate CCR data 
     */
    public function __call($name, $args)
    {
        $matches = array();

        if (substr($name, 0, 3) === 'get') {
            $category = substr($name, 3);

            switch ($category) {
                case 'Conditions':
                    $category = 'Problems';
                    break;
                case 'Allergies':
                    $category = 'Alerts';
                    break;
                case 'TestResults':
                    // TestResults is an alias for LabResults
                case 'LabResults':
                    $category = 'Results';
                    break;
                default:
                    // $category is already well formatted
            }

            for ($i = 0; $i < count($this->_extensionElements); $i++) {
                if ($this->_extensionElements[$i]->_rootElement == 'Body') {
                    for ($j = 0; $j < count($this->_extensionElements[$i]->_extensionElements); $j++) {
                        if ($this->_extensionElements[$i]->_extensionElements[$j]->_rootElement == $category) {
                            $matches[] = &$this->_extensionElements[$i]->_extensionElements[$j];
                        }
                    }
                }
            }
        }

        return isset($matches[0]) ? $matches[0]->_extensionElements : array();
    }
}
