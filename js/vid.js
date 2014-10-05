var $player = $('#player');
			var player = $player.get(0);
			var $parent = $player.parent();
			var $win = $(window);
			var resizeTimeout = null;
			var shouldResize = true;
			var shouldPosition = true;
			var videoRatio = 16 / 9;

			player.volume = 0; // mute

			var resize = function() {
				if (!shouldResize) { return; }

				var height = $parent.height();
				var width = $parent.width();
				var viewportRatio = width / height;
				var scale = 1;

				if (videoRatio < viewportRatio) {
					// viewport more widescreen than video aspect ratio
					scale = viewportRatio / videoRatio;
				} else if (viewportRatio < videoRatio) {
					// viewport more square than video aspect ratio
					scale = videoRatio / viewportRatio;
				}

				var offset = positionVideo(scale, width, height);
				setVideoTransform(scale, offset);
			};

			var setVideoTransform = function(scale, offset) {
				offset = $.extend({ x: 0, y: 0 }, offset);
				var transform = 'translate(' + Math.round(offset.x) + 'px,' + Math.round(offset.y) + 'px) scale(' + scale  + ')';
				$player.css({
					'-webkit-transform': transform,
					'transform': transform
				});
			};

			// accounts for transform origins on scaled video
			var positionVideo = function(scale, width, height) {
				if (!shouldPosition) { return false; }

				var x = parseInt($player.data('origin-x'), 10);
				var y = parseInt($player.data('origin-y'), 10);
				setVideoOrigin(x, y);

				var viewportRatio = width / height;
				var scaledHeight = scale * height;
				var scaledWidth = scale * width;
				var percentFromX = (x - 50) / 100;
				var percentFromY = (y - 50) / 100;
				var offset = {};

				if (videoRatio < viewportRatio) {
					offset.x = (scaledWidth - width) * percentFromX;
				} else if (viewportRatio < videoRatio) {
					offset.y = (scaledHeight - height) * percentFromY;
				}

				return offset;
			};

			var setVideoOrigin = function(x, y) {
				var origin = x + '% ' + y + '%';
				$player.css({
					'-webkit-transform-origin': origin,
					'transform-origin': origin
				});
			};

			$win.on('resize', function() {
				clearTimeout(resizeTimeout);
				resizeTimeout = setTimeout(resize, 100);
			});