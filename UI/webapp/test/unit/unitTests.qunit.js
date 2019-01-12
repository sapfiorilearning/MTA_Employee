/* global QUnit */
QUnit.config.autostart = false;

sap.ui.getCore().attachInit(function () {
	"use strict";

	sap.ui.require([
		"com/sl/UI/test/unit/AllTests"
	], function () {
		QUnit.start();
	});
});