# ImmersivePoC

Android proof of concept project for going [Fullscreen / Immersive Mode](https://developer.android.com/training/system-ui/immersive).

### Types of Immersive Mode

First, understanding the different types of fullscreen modes. There are 3 behaviors your Activity can adopt. While all 3 involve hiding the system bars (status bar and navigation bar), each has a different way of bringing them back:  

1. **Lean back**: Tapping on the screen once will bring the system bars back. Video players on mobile devices typically have this approach.  
Note: This isn't mentioned in the docs, but starting with *API 30*, the behavior is slightly different for Lean back mode.  
On *API29* both status bar and navigation bar are brought back.  
On *API30* only the navigation bar is brought back. You have to manually swipe down where the status bar would normally be to make it come back.  

2. **Immersive**: A more strict version of **Lean back**, the system bars will appear only after swipping the space where a system bar would be. This would be suited for games or reading apps, since the user won't trigger the system bars while interacting with the app. The touch gesture won't be registered by the app.

3. **Sticky Immersive**: The even more stricter version, the system bars will only shortly appear when swipping in their area, and they're semi-transparent. If the app requires swipping where the system bars are, you should use this mode. The touch gesture will be passed to the app, so it may respond to it too.
