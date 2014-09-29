/**
 * oLoader v0.1
 *	@author: Jakub Sedlacek
 *	@website: http://js.ownage.sk/
 *	@project website: http://projects.ownage.sk/jquery/oLoader 
 **/
(function($) {

    if (!$.ownage) {
        $.ownage = new Object();
    }

    $.ownage.Loader = function(el, params, selector, number) {

        var base = this;

        base.el = el;
        base.$el = $(el);
        base.selector = selector;
        base.context = base.$el;
        base.checkInterval = false;
        
        if (!$.ownage.Loader.objects) {
            $.ownage.Loader.objects = new Array();
            $.ownage.Loader.objectOptions = new Array();
        }
        base.objects = $.ownage.Loader.objects;

        var data = new Object();

        //all methods start here
        base.setObject = function() {            
            base.objects.push(base.selector);
            base.objectIndex = base.getObjectIndex();
            
            data.params = $.ownage.Loader.objectOptions[base.objectIndex];
            base.params = data.params;

            base.overlaySel = "oloader_overlay_" + base.objectIndex;
            base.canvasSel = "oloader_canvas_" + base.objectIndex;
            base.loaderSel = "oloader_loader_" + base.objectIndex;
            base.isSet = true;
        }
        
        base.init = function() {

            base.setObject();
            
            if (typeof params === "string") {
                switch (params) {
                    case "preloadImages":
                        base.preloadImages();
                        break;
                    case "show":    
                        base.show();
                        break;
                    case "hide":
                        base.hide();
                        break;
                    case "adjust":
                        base.adjustLoader();
                        break;
                }
                return;
            } else {
                $.ownage.Loader.objectOptions[base.objectIndex] = {};
                $.extend($.ownage.Loader.objectOptions[base.objectIndex], $.ownage.Loader.defaultOptions, params);
                base.params = $.ownage.Loader.objectOptions[base.objectIndex];
                if (base.params.showOnInit === true) {
                    
                    base.show();
                }
            }           
        };
        
        base.executeOnStart = function() {
            if(typeof base.params.onStart === "function") {
                base.params.onStart();
            }
        }
        
        base.getObjectIndex = function() {
            for (var i = 0; i < base.objects.length; i++) {
                if (base.selector === base.objects[i])
                    return i + '_' + number;
            }
        };
        
        base.removeCanvas = function() {
          $('#' + base.canvasSel).remove();
        };
        
        base.adjustLoader = function() {
            var pos = base.context.offset();
            var w = base.context.outerWidth(true);
            var h = base.context.outerHeight(true);
            
            if($(base.selector).length==0) {
              base.removeCanvas();
            }
            if(pos.left==base.lastPosLeft && pos.top==base.lastPosTop
              && w==base.lastWidth && h==base.lastHeight && base.lastLoaderWidth == $('#' + base.loaderSel).outerWidth()) {
              return;
            }
            var myPosition = 'absolute';
            if (base.params.wholeWindow) {
                w = $(window).width();
                h = $(window).height();
                var pos = new Object();
                pos.left = 0;
                pos.top = 0;
                myPosition = 'fixed';
            }
            
            base.lastPosLeft = pos.left;
            base.lastPosTop = pos.top;
            base.lastWidth = w;
            base.lastHeight = h;
            base.lastLoaderWidth = $('#' + base.loaderSel).outerWidth();
            
            

            $('#' + base.overlaySel).css({
                //background: base.params.backgroundColor,
                position: myPosition,
                left: '0px',
                top: '0px',
                width: w,
                height: h
            });
            $('#' + base.canvasSel).css({
                position: myPosition,
                left: pos.left + 'px',
                top: pos.top + 'px',
                width: w,
                height: h,
                zIndex: 100
            });
            $('#' + base.loaderSel).css({
                position: myPosition,
                left: '50%',
                top: '50%',
                marginLeft: -parseInt($('#' + base.loaderSel).outerWidth() / 2),
                marginTop: -parseInt($('#' + base.loaderSel).outerHeight() / 2)
            });
        }
        
        base.setCheckInterval = function() {
          $('#' + base.overlaySel).css({
                background: base.params.backgroundColor
          });
          base.checkInterval = setInterval(function(){
            base.adjustLoader();
          },base.params.checkIntervalTime);
        }
        
        base.show = function() {

            if ($('#' + base.canvasSel).length > 0)
                return;

            if (base.context.parent().hasClass('oloader_canvas') ||
                    base.context.hasClass('oloader_canvas')
                    )
                return;
                           
            $('body').append("<div id='" + base.canvasSel + "' class='oloader_canvas' style='padding:0px;margin:0px;display:none;overflow:hidden'></div>");

            if (base.params.modal) {
                $('#' + base.canvasSel).append("<div id='" + base.overlaySel + "'></div>");
                $('#' + base.overlaySel).hide().fadeTo(0, base.params.fadeLevel);
            }

            if (base.params.showLoader) {
                $('#' + base.canvasSel).append(base.setStyle());
            }
            $('#' + base.canvasSel).hide().fadeTo(base.params.fadeInTime, 1);
            
            base.lockOverflow();
            base.setCheckInterval();
            base.executeOnStart();
            base.adjustLoader();
            base.executeEffect(1);
            var t;
            if (base.params.hideAfter !== 0) {
                t = setTimeout(function() {
                    base.hide();
                }, base.params.hideAfter);
            }

            if (base.params.waitLoad) {
                base.context.load(function() {
                    base.hide();
                });
            }

            if (base.params.url !== false) {
                $.ajax({
                    url: base.params.url,
                    type: base.params.type,
                    data: base.params.data,
                    success: function(data) {
                        base.params.successData = data;
                        if (!base.params.hideAfter) {
                            if (!base.params.updateOnComplete) {
                                base.updateContent();
                            }
                            base.hide();
                        }
                    },
                    error: function(jqXHR,textStatus,errorThrown) {
                      if(typeof(base.params.onError)=="function") {
                        base.params.onError(jqXHR,textStatus,errorThrown);
                      }
                    }
                });
            }
        };
        
        base.hide = function() {
            var done = false;
            
            base.executeEffect(false);
            $('#' + base.canvasSel).fadeTo(base.params.fadeOutTime, 0, function() {
                //On complete                
                if (!done) {
                    done = true;
                    sucData = "undefined";
                    if (typeof base.params.complete === "function") {
                        if (base.params.successData)
                            sucData = base.params.successData;
                        base.params.complete(sucData);
                    }
                    if (base.params.updateOnComplete) {
                        base.updateContent();
                    }
                }
                clearInterval(base.checkInterval);
                $(this).remove();
                base.lockOverflow(true);
            });
        };

        base.updateContent = function() {
            sucData = "Load failed.";
            if (base.params.successData)
                sucData = base.params.successData;
            if (base.params.updateContent && base.params.url !== false) {
                base.context.html(sucData);
            }
            base.adjustLoader();
        };
        
        base.lockOverflow = function(show) {
          if(base.params.lockOverflow) {
            if(!show) {
              $(base.params.context).css('overflow','hidden');
            } else {
              $(base.params.context).css('overflow','auto');
            }
          }
        }
        

        base.setStyle = function() {
            var text = "";

            if (typeof base.params.style === "string") {
                return "<div id='" + base.loaderSel + "' style='position:absolute;left:0;top:0;width:100%;height:100%;'>" + base.params.style + "</div>";
            }

            switch (base.params.style) {
                case 4:
                    text = "<img src='" + base.params.image + "' id='" + base.loaderSel + "' style='box-shadow:0px 0px 20px #000;position:absolute;padding:" + base.params.imagePadding + "px;background:" + base.params.imageBgColor + "' alt='Loading...' />";
                    break;
                case 3:
                    text = "<img src='" + base.params.image + "' id='" + base.loaderSel + "' style='box-shadow:0px 0px 20px #222;border-radius:1000px;position:absolute;padding:" + base.params.imagePadding + "px;background:" + base.params.imageBgColor + "' alt='Loading...' />";
                    break;
                case 2:
                    text = "<div id='" + base.loaderSel + "' style='position:absolute;width:100%;padding-top:10px;padding-bottom:10px;";
                    text += "text-align:center;background-color:" + base.params.imageBgColor + "'><img src='" + base.params.image + "' alt='Loading...' /></div>";
                    break;
                case 0:
                    text = "<img src='" + base.params.image + "' id='" + base.loaderSel + "' style='position:absolute' alt='Loading...' />";
                    break;
                case 1:
                default:
                    text = "<img src='" + base.params.image + "' id='" + base.loaderSel + "' style='box-shadow:1px 1px #000;border-radius:1000px;position:absolute;padding:" + base.params.imagePadding + "px;background:" + base.params.imageBgColor + "' alt='Loading...' />";
                    break;
            }
            return text;
        };
          
        //executeEffect - show parameter determines whether it is on show(true) or on hide(false)
        base.executeEffect = function(show) {
            switch (base.params.effect) {
                case "doornslide":
                case "door":
                    var myid = "oloader_effect_door_" + base.objectIndex;
                    var myid2 = "oloader_effect_door_2_" + base.objectIndex;
                    myData = {
                      backgroundColor: base.params.backgroundColor,
                      fadeLevel: base.params.fadeLevel
                    };
                    if(base.params.effectData) {
                      myData = base.params.effectData;
                    }
                    
                    if (show) {
                        $('#' + base.overlaySel).after("<div id='" + myid + "'></div>");
                        $('#' + myid).css({
                            position: 'absolute',
                            overflow: 'hidden',
                            top: '50%',
                            left: '0px',
                            width: '100%',
                            height: '0px',
                            marginTop: '0px',
                            'box-shadow': '0px 0px 25px ' + myData.backgroundColor,
                            background: myData.backgroundColor,//base.params.backgroundColor,
                            opacity: 0
                        }).animate({
                            height: '150px',
                            marginTop: '-75px',
                            opacity: myData.fadeLevel
                        }, base.params.fadeInTime + 250, 'easeOutBounce');
                    } else {
                        $('#' + myid).animate({
                            height: '0px',
                            marginTop: '0px'
                        }, base.params.fadeOutTime + 250, 'easeOutBounce');
                    }
                    if (base.params.effect !== "doornslide") {
                        break;
                    }

                case "slide":
                    var l = $('#' + base.overlaySel).css('left');
                    if (show) {
                        $('#' + base.overlaySel).css('left', -$('#' + base.overlaySel).width());
                    } else {
                        l = $('#' + base.overlaySel).width();
                    }

                    $('#' + base.overlaySel).animate({
                        left: l
                    }, (show ? base.params.fadeInTime : base.params.fadeOutTime));
                    break;
            }
        };

        base.preloadImages = function() {
            var images = [
                'images/ownageLoader/loader1.gif',
                'images/ownageLoader/loader2.gif',
                'images/ownageLoader/loader3.gif',
                'images/ownageLoader/loader4.gif',
                'images/ownageLoader/loader5.gif',
                'images/ownageLoader/loader6.gif',
                'images/ownageLoader/loader7.gif'
            ];

            var text = "";

            for (var i = 0; i < images.length; i++) {
                text += "<img src='" + images[i] + "' alt='Loading...' />";
            }

            $('body').append("<div class='oloader_image_preload' style='position:absolute;left:-5000px;top:-5000px;'>" + text + "</div>");
            $('.oloader_image_preload').hide();
        };

        base.init();

    };

    $.ownage.Loader.defaultOptions = {
        image: 'images/ownageLoader/loader1.gif',
        style: 1,
        context: 'body',
        modal: true,
        fadeInTime: 300,
        fadeOutTime: 300,
        fadeLevel: 0.7,
        backgroundColor: '#000',
        imageBgColor: '#fff',
        imagePadding: '10',
        showOnInit: true,
        hideAfter: 0,
        url: false,
        type: 'GET',
        data: false,
        updateContent: true,
        updateOnComplete: true,
        showLoader: true,
        effect: '',
        wholeWindow: false, //use for whole page coverage
        lockOverflow: false, //locks body's overflow when loading
        waitLoad: false, //useful for images
        checkIntervalTime: 100,
        
        //functions
        complete: '', //on complete
        onStart: '', //executes when animation starts
        onError: ''  //executes if an error from ajax request is thrown
    };  

    $.fn.oLoader = function(params) {
        var sel = this.selector;
        var num = 0;
        return this.each(function() {
            num++;
            (new $.ownage.Loader(this, params, sel, num));
        });
    };
		
/**
 * oPageLoader bonus
 *	@author: Jakub Sedlacek
 *	@website: http://js.ownage.sk/
**/		
		$.ownage.PageLoader = function(params) {
			
			var base = this;
			
			base.options = {};
			$.extend(base.options,$.ownage.PageLoader.defaultOptions,params);
			
			base.init = function() {
				
        base.additionalImages();
				base.done = false;
				base.loaded = 0;
				base.total = $(base.options.affectedElements).length;
        
        //if total of elements to be checked is zero, nothing has to be done
        if(base.total==0) return;
        
        if(base.options.lockOverflow) {
          $(base.options.context).css('overflow','hidden');
        }
        
				$(base.options.context).oLoader({
					wholeWindow: base.options.wholeWindow,
					backgroundColor: base.options.backgroundColor,
					fadeInTime:0,
					fadeOutTime:base.options.fadeOutTime,
					fadeLevel: base.options.fadeLevel,
					style: base.options.style,
          complete: base.options.complete
				});
        
        if(!base.options.ownStyle) {
  				$('#ownage_page_loader_text').css({
  					position: 'absolute',
            display: (base.options.showPercentage) ? 'block' : 'none',
  					left:'50%',
  					top: '50%',
  					color: base.options.percentageColor,
  					fontSize: base.options.percentageFontSize,
            zIndex: '1000'
  				}).css({
  					marginTop: -(base.options.progressBarHeight/2) - ($('#ownage_page_loader_text').height())
  				});
  				$('#ownage_page_loader').css({
            position: 'absolute',
  					top: '50%',
  					left: '0px',
  					background: base.options.progressBarColor,
  					height: base.options.progressBarHeight,
  					marginTop: -(base.options.progressBarHeight/2)
  				});
        }
        $('#ownage_page_loader').fadeTo(0,base.options.progressBarFadeLevel);
				$(base.options.affectedElements).load(function(){
					if(base.done === false) {
						base.loaded++;	
						base.updateProgressBar();
					}
				});
				
				$(window).load(function(){
					base.done = true;
					base.loaded = base.total;
					
          if(typeof base.options.completeLoad == "function") {
            base.options.completeLoad();
          }
          
          base.updateProgressBar();
          
				});
				
			}
      
      base.additionalImages = function() {
        if(base.options.images.length==0) return;
        $('body').append("<div style='position:absolute;left:-10000px;top:-10000px;display:none;' id='ownage_page_loader_addImages'></div>");
        for(i=0;i<base.options.images.length;i++) {
          $('#ownage_page_loader_addImages').append("<img src='"+base.options.images[i]+"' />");
        }
      }
			
			base.updateProgressBar = function() {
				var perc = parseInt((base.loaded/base.total)*100);
				
        if(perc == base.lastPercentage) return;
        base.lastPercentage = perc;
        
				$('#ownage_page_loader_text').html(perc+'%');
        if(!base.options.ownStyle) {
  				$('#ownage_page_loader_text').css({
  					marginLeft:-($('#ownage_page_loader_text').width()/2)
  				});
        }
				$('#ownage_page_loader').stop().animate({
					width: perc+'%'
				},100);
				
        if(typeof base.options.update == "function") {
          base.options.update({
            loaded: base.loaded,
            total: base.total,
            percentage: perc
          });
        }
        
				if(perc === 100) {
            setTimeout(function(){
              $(base.options.context).oLoader('hide');
              if(base.options.lockOverflow) {
                $(base.options.context).css('overflow','auto');
              }
            },base.options.waitAfterEnd);
				}
			}
			
			base.init();
			
		}; 
		 
		$.ownage.PageLoader.defaultOptions = {
			backgroundColor: '#000',
			progressBarColor: '#f00',
			progressBarHeight: 3,
      progressBarFadeLevel: 1,
      showPercentage: true,
			percentageColor: '#fff',
			percentageFontSize: '30px',
			context: 'body',
			affectedElements: 'img,iframe,frame,script',
      ownStyle: false,
      style: "<div id='ownage_page_loader_text'>0%</div><div id='ownage_page_loader'></div>",
      lockOverflow: true,
      images: [], //array of additional images, such as those from css files
			
			wholeWindow: true,
			fadeLevel: 1,
      waitAfterEnd: 0,
			fadeOutTime: 500,
      
      //callbacks
      complete: false, //calls after page is loaded and animation ends
      completeLoad: false, //calls after page is loaded and doesn't wait till animation is over
      update: false
		};
		
		$.oPageLoader = function(params) {
			$.ownage.PageLoader(params);
		};

})(jQuery);