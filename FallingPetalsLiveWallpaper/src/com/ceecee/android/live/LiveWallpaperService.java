package com.ceecee.android.live;


import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;

import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.emitter.PointParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.BlendFunctionParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.initializer.GravityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.RotationParticleModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;

import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;

import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class LiveWallpaperService extends BaseLiveWallpaperService implements IAccelerationListener{

//================================================================================
//                                  Fields
//================================================================================
	private static final int MAX_FRAMES_PER_SECOND = 16;
	
	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 720;
	
    private Camera mCamera;
    private Scene mScene;

	private ITextureRegion mFlowerTextureRegion;	
	private BitmapTextureAtlas mFlowerTexture;	
	private VelocityParticleInitializer<UncoloredSprite> mVelocityParticleInitializer;

	
	@Override
	public EngineOptions onCreateEngineOptions() {
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		wm.getDefaultDisplay().getRotation();
		CAMERA_WIDTH = displayMetrics.widthPixels;
		CAMERA_HEIGHT = displayMetrics.heightPixels;
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback  createResourcesCallback) throws Exception {
        this.mFlowerTexture = new BitmapTextureAtlas(this.getTextureManager(),64,64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);       
        this.mFlowerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFlowerTexture, this, "gfx/rosetrans64.png",0,0);      
        this.getEngine().getTextureManager().loadTexture(this.mFlowerTexture);
    	this.enableAccelerationSensor(this);
        createResourcesCallback.onCreateResourcesFinished();
	}
	@Override
	public org.andengine.engine.Engine onCreateEngine(final
		EngineOptions pEngineOptions)
	{
		return new LimitedFPSEngine(pEngineOptions, MAX_FRAMES_PER_SECOND);
		
	}


	@Override
	public void onCreateScene(OnCreateSceneCallback createSceneCallback) throws Exception {		
		mScene= new Scene();

//add the background to the scene 
// I chose a black background to accentuate the red rose color
		mScene.setBackground(new Background(0.0f, 0.0f, 0.0f));	
		
// set the x y values of where the petals fall from
		final int mParticleX = CAMERA_WIDTH/2;
		final int mParticleY = 0;
//Set the max and min rates that particles are generated per second
		final int mParticleMinRate = 1;
		final int mParticleMaxRate = 2;
//Set a variable for the max particles in the system.
		final int mParticleMax = 40;
		
/* Create Particle System. 
 * Changed to BatchedSpriteParticleSystem to improve performance
 *  and reduce battery usage*/	
		final BatchedSpriteParticleSystem particleSystem = new BatchedSpriteParticleSystem
				(new PointParticleEmitter(mParticleX, mParticleY), 
						mParticleMinRate, mParticleMaxRate, mParticleMax,
						this.mFlowerTextureRegion, this.getVertexBufferObjectManager());
						
			particleSystem.addParticleInitializer(new BlendFunctionParticleInitializer<UncoloredSprite>(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE));
//			set initial velocity		
			this.mVelocityParticleInitializer = new VelocityParticleInitializer<UncoloredSprite>(-100, 100, 20, 190);
			particleSystem.addParticleInitializer(this.mVelocityParticleInitializer);
			
//			add gravity so the particles fall downward
			particleSystem.addParticleInitializer(new GravityParticleInitializer<UncoloredSprite>());
//			add acceleration so particles float 
			particleSystem.addParticleInitializer(new AccelerationParticleInitializer<UncoloredSprite>(0, -10));
//			add a rotation to particles
			particleSystem.addParticleInitializer(new RotationParticleInitializer<UncoloredSprite>(0.0f, 90.0f));
//			have particles expire after 40
			particleSystem.addParticleInitializer(new ExpireParticleInitializer<UncoloredSprite>(40.0f));

//			change rotation of particles at various times
			particleSystem.addParticleModifier(new RotationParticleModifier<UncoloredSprite>(0.0f, 10.0f, 0.0f, -180.0f));
			particleSystem.addParticleModifier(new RotationParticleModifier<UncoloredSprite>(10.0f, 20.0f, -180.0f, 90.0f));
			particleSystem.addParticleModifier(new RotationParticleModifier<UncoloredSprite>(20.0f, 30.0f, 90.0f, 0.0f));
			particleSystem.addParticleModifier(new RotationParticleModifier<UncoloredSprite>(30.0f, 40.0f, 0.0f, -90.0f));
//			add some fade in and fade out to the particles
			particleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(0.0f,10.f,0.0f, 1.0f));
			particleSystem.addParticleModifier(new AlphaParticleModifier<UncoloredSprite>(25.0f, 40.0f, 1.0f, 0.0f));

//			attach particle system to scene
			this.mScene.attachChild(particleSystem);
		
			createSceneCallback.onCreateSceneFinished(mScene);
	
}		

	@Override
	public void onPopulateScene(Scene arg0, OnPopulateSceneCallback populateSceneCallback)
			throws Exception {
		populateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}
// Change the petals to move along the axes of the accelerometer
	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final float minVelocityX = (pAccelerationData.getX() + 2) * 2;
		final float maxVelocityX = (pAccelerationData.getX() - 2) * 2;	
		final float minVelocityY = (pAccelerationData.getY() - 4) * 5;
		final float maxVelocityY = (pAccelerationData.getY() - 6) * 5;
		this.mVelocityParticleInitializer.setVelocity(minVelocityX, maxVelocityX, minVelocityY, maxVelocityY);	
	}
	}

