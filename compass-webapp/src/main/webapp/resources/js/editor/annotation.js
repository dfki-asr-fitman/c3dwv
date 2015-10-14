/* global XML3D, COMPASS, annotationEditDialog */

XML3D.tools.namespace("COMPASS");

(function() {
"use strict";
	COMPASS.Annotations = new XML3D.tools.Singleton({
		_isPlacing: false,
		_currentlyEditingAnnotation: null,
		_isVisible: true,
		_lastHitEvent: null,
		_isGrowlsEnabled: false,
		COMPONENT_NAME: "de.dfki.asr.compass.fitman.model.components.AnnotationComponent",
		_annotations: {},

		initialize: function() {
			this.setAllAnnotationGeometryVisible(true);
		},

		onToggleButtonClick: function() {
			if (this._isVisible) {
				this.setAllAnnotationGeometryVisible(false);
				this._isVisible = false;
			} else {
				this.setAllAnnotationGeometryVisible(true);
				this._isVisible = true;
			}
			this._setButtonState();
		},

		areAnnotationsVisible: function() {
			return this._isVisible;
		},

		setAllAnnotationGeometryVisible: function(visibilityState) {
			var annotationNodes = $("group[data-annotation]");
			for(var idx=0; idx<annotationNodes.length; idx++) {
				var annotationGroup = annotationNodes[idx];
				($(annotationGroup)).attr("visible", visibilityState.toString());
			}
		},

		onPlaceButtonClick: function() {
			if (this.isPlacingAnnotation()) {
				this.disablePlacement();
			} else {
				this.enablePlacement();
			}
		},

		_setButtonState: function(){
			//placement button
			if(this.isPlacingAnnotation()){
				$("button[id$='annotationButton']").addClass("compass-button-active");
			}else{
				$("button[id$='annotationButton']").removeClass("compass-button-active");
			}

			//visibility button
			if(this.areAnnotationsVisible()){
				$("button[id$='annotationToggleButton']").addClass("compass-button-active");
			}else{
				$("button[id$='annotationToggleButton']").removeClass("compass-button-active");
			}
		},

		enablePlacement: function() {
			this._isPlacing = true;
			this._setButtonState();
		},

		disablePlacement: function() {
			this._isPlacing = false;
			this._setButtonState();
		},

		isPlacingAnnotation: function() {
			return this._isPlacing;
		},

		isEditing: function() {
			return this._currentlyEditingAnnotation !== null;
		},

		enableEditing: function(annotationId) {
			this.disablePlacement();
			this._currentlyEditingAnnotation = annotationId;
			var annotation = this._annotations[annotationId];
			this.getTextInput().val(annotation.text);
			this.getTitleInput().val(annotation.title);
		},

		clearEditing: function() {
			this._currentlyEditingAnnotation = null;
		},

		createClickHandler: function(originalHandler) {
			var handlerFunction = function(mouseEvent) {
				if (COMPASS.Annotations.isPlacingAnnotation()) {
					COMPASS.Annotations.placeAnnotationAt(mouseEvent);
				} else {
					originalHandler.apply(this, arguments);
				}
			};
			return handlerFunction;
		},

		placeAnnotationAt: function (mouseEvent) {
			var hit = COMPASS.EventHandler._getMouseHitInformation(mouseEvent.clientX, mouseEvent.clientY);
			if (hit.target === null) {
				// for some reason, the object has vanished between the click event
				// and our getMouseHitInformation query.
				console.log("Object hit has vanished while placing annotation. Ignoring.");
				return;
			}
			this._lastHitEvent = hit;
			annotationEditDialog.show();
		},

		confirmAnnotationDialog: function() {
			if (this.isPlacingAnnotation()) {
				this.confirmAnnotationPlacement();
			} else if (this.isEditing()) {
				this.confirmAnnotationEdit();
			}
		},

		confirmAnnotationPlacement: function() {
			var command = {
				sceneNodeId: this.getTargetSceneNodeId(),
				title: this.getTitleInput().val(),
				text: this.getTextInput().val(),
				position: this._lastHitEvent.point.str(),
				normal: this._lastHitEvent.normal.str()
			};
			var jsfCommand = COMPASS.RemoteCaller.toJSFParameterList(command);
			remoteAttachAnnotation(jsfCommand);
			this.clearDialogFields();
			annotationEditDialog.hide();
		},

		confirmAnnotationEdit: function() {
			var command = {
				annotationId: this._currentlyEditingAnnotation,
				title: this.getTitleInput().val(),
				text: this.getTextInput().val()
			};
			var jsfCommand = COMPASS.RemoteCaller.toJSFParameterList(command);
			remoteEditAnnotation(jsfCommand);
			this.clearEditing();
			this.clearDialogFields();
			annotationEditDialog.hide();
		},

		abortAnnotationDialog: function() {
			this._lastHitEvent = null;
			this.clearEditing();
			this.clearDialogFields();
			annotationEditDialog.hide();
		},

		getTitleInput: function() {
			return $("#annotation-title-input");
		},

		getTextInput: function() {
			return $("#annotation-text-input");
		},

		clearDialogFields: function() {
			this.getTitleInput().val("");
			this.getTextInput().val("");
		},

		getTargetSceneNodeId: function() {
			if (this._lastHitEvent === null) {
				console.log("Trying to determine scene node when no hit was recorded! Aiiie!");
				throw "error";
			}
			var $target = $(this._lastHitEvent.target);
			var PREFIX = COMPASS.XML3DProducer.prototype.SCENENODE_ID_PREFIX;
			var mangledNodeId = $target
					.parents("group[id^='"+PREFIX+"']")
					.attr("id");
			return this.stripPrefix(mangledNodeId, PREFIX);
		},

		stripPrefix: function(string, prefix) {
			if (string.indexOf(prefix) !== 0) {
				// string does not start with prefix.
				return string;
			}
			var remainingLength = string.length - prefix.length;
			return string.substr(prefix.length, remainingLength);
		},

		handleComponent: function(component) {
			if (component.type !== this.COMPONENT_NAME) return;
			var $group = COMPASS.Editor.xml3dProducer.findGroupForSceneNodeId(component.owner);
			var $ourgroup = this.ensureSubGroup($group, component);
			var scale = this.getMarkerScale($group);
			this.checkForDeletedAnnotations($ourgroup, component);
			for (var idx in component.annotations) {
				var annotation = component.annotations[idx];
				// save/update for later use
				this.triggerGrowl(annotation);
				this._annotations[annotation.id] = annotation;
				var $meshGroup = this.ensureMeshGroup($ourgroup, annotation);
				this.setTransform($meshGroup, annotation, scale);
			}
		},

		ensureSubGroup: function($parentGroup, component) {
			var children = $parentGroup.children("[data-componentid="+component.id+"]");
			var $node;
			if (children.length < 1) {
				$node = $(XML3D.tools.creation.element("group"));
				$node.attr("data-componentid", component.id);
				$parentGroup.append($node);
			} else {
				$node = children[0];
			}
			return $($node);
		},

		ensureMeshGroup: function($ourgroup, annotation) {
			var children = $ourgroup.children("[data-annotation="+annotation.id+"]");
			var $node;
			if (children.length < 1) {
				$node = $(XML3D.tools.creation.element("group"));
				$node.append(this.makeMesh());
				$node.attr("data-annotation", annotation.id);
				$node.attr("visible", this.areAnnotationsVisible().toString());
				$node.on("click", this.makeClickHandler(annotation));
				$ourgroup.append($node);
			} else {
				$node = children[0];
			}
			return $($node);
		},

		getMarkerScale: function($parentGroup) {
			return 10;
		},

		setTransform: function($meshGroup, annotation, scale) {
			var scaling = "scale3d("+scale+", "+scale+", "+scale+") ";
			var t = annotation.position;
			var translate = "translate3d("+t.x+"px, "+t.y+"px, "+t.z+"px) ";
			var r = this.quaternionToAxisAngle(annotation.localRotation);
			var rotate = "rotate3d("+r.axis.x+", "+r.axis.y+", "+r.axis.z+", "+r.angle+"rad)";
			$meshGroup.attr("style", "transform: "+translate+rotate+scaling+";");
		},

		quaternionToAxisAngle: function (rotation) {
			var vector = new XML3DVec3(rotation.x, rotation.y, rotation.z);
			var r = new XML3DRotation();
			r.setQuaternion(vector, rotation.w);
			return r;
		},

		checkForDeletedAnnotations: function($containingGroup, component) {
			var $annotationGroups = $containingGroup.children();
			$annotationGroups.each(function (idx, el) {
				var annotationId = $(this).data("annotation");
				var stillThere = component.annotations.some(
					function(a){ return a.id == annotationId; }
				);
				if (!stillThere) {
					$(this).remove();
					COMPASS.Annotations.annotationDeleted(annotationId);
				}
			});
		},

		makeMesh: function() {
			return XML3D.tools.creation.element("model", {
				"src": "../plugins/nubsi.xml#nubsi"
			});
		},

		makeClickHandler: function(annotation) {
			var closuredAnnotationId = annotation.id;
			return function(event) {
				COMPASS.Annotations.annotationClicked(closuredAnnotationId);
			};
		},

		annotationClicked: function(annotationId) {
			if (!this._annotations[annotationId]) {
				console.log("Unknown annotation clicked. Aiiie!");
			}
			this.enableEditing(annotationId);
			annotationEditDialog.show();
		},

		annotationDeleted: function(annotationId) {
			this.showGrowl("Annotation deleted:", this._annotations[annotationId]);
			delete this._annotations[annotationId];
		},

		enableGrowls: function() {
			this._isGrowlsEnabled = true;
		},

		triggerGrowl: function(annotation) {
			var saved = this._annotations[annotation.id];
			if (saved === undefined) {
				this.showGrowl("Annotation created:", annotation);
			} else {
				if ((saved.text !== annotation.text)
					|| (saved.title !== annotation.title)) {
					this.showGrowl("Annotation changed:", annotation);
				}
			}
		},

		showGrowl: function(statusText, annotation) {
			if (!this._isGrowlsEnabled) { return; }
			var contents = this.formatGrowl(statusText, annotation);
			this._growl(contents);
		},

		formatGrowl: function(statusText, annotation) {
			return statusText+"<br/>"+
					"<b>"+annotation.title+"</b><br/>"+
					annotation.text;
		},

		_growl : function(content) {
			var type = "info";
			$.bootstrapGrowl(content, {
				ele: 'body', // which element to append to
				type: type, // (null, 'info', 'danger', 'success')
				offset: {from: 'top', amount: 20}, // 'top', or 'bottom'
				align: 'right', // ('left', 'right', or 'center')
				width: 250, // (integer, or 'auto')
				delay: 4000, // Time while the message will be displayed.
				allow_dismiss: true, // If true then will display a cross to close the popup.
				stackup_spacing: 10 // spacing between consecutively stacked growls.
			});
		}
	});

	$.aop.after({target: COMPASS.XML3DProducer, method: "_createMeshClickHandler"},
		function (originalHandler) {
			return COMPASS.Annotations.createClickHandler(originalHandler);
		}
	);

	$.aop.before({target: COMPASS.XML3DProducer, method: "_convertSceneNodeComponent"},
		function (args) {
			COMPASS.Annotations.handleComponent(args[0]);
		}
	);

	$.aop.before({target: COMPASS.EventHandler, method: /onSceneNode.*Event/},
		function() {
			COMPASS.Annotations.enableGrowls();
		}
	);
})();
