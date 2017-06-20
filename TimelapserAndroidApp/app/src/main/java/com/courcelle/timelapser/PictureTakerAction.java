package com.courcelle.timelapser;

/**
 * Created by tubed on 20/06/2017.
 */

public class PictureTakerAction implements IAction {
    private PictureTaker pictureTaker;

    public PictureTakerAction(PictureTaker pictureTaker) {
        this.pictureTaker=pictureTaker;
    }

    @Override
    public void run() {
        pictureTaker.takePicture();
    }
}
