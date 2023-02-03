import {
  Box,
  Button,
  CircularProgress,
  Grid,
  Typography,
} from "@material-ui/core";
import { API } from "aws-amplify";
import { useSnackbar } from "notistack";
import React, { FC, FormEvent, useCallback, useState } from "react";
import { useRecoilState, useRecoilValue } from "recoil";
import { useDropzone } from "react-dropzone";

import ImageUploadProps from "./types";
import useImageUploadStyles from "./style";
import config from "../../config";
import { userAtom } from "state";
import { SUCCESS_TOAST_OPTIONS, ERROR_TOAST_OPTIONS } from "../../constants";
import { FaCloudUploadAlt } from "react-icons/fa";

const acceptedTypes = ["image/png", "image/jpg", "image/jpeg"];
let PATH = "/entity/upload-";

const getBase64 = async (file: File) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = (error) => reject(error);
  });

export const ImageUpload: FC<ImageUploadProps> = ({
  callback,
  type,
  handleLogoChange,
}) => {
  const [uploading, setUploading] = useState(false);
  const [userAtomState, setUserAtomState] = useRecoilState(userAtom);
  const onDrop = useCallback(
    async (acceptedFiles) => {
      try {
        setUploading(true);
        const [file] = acceptedFiles;
        const base64File = await getBase64(file);
        const payload = {
          userId: userId,
          base64ImageFile: base64File,
          mimeType: file.type,
        };
        if (handleLogoChange) {
          handleLogoChange(base64File);
        } else {
          await API.post(
            config.apiGateway.NAME,
            `${PATH}${type.toLowerCase()}`,
            {
              body: payload,
            }
          );
        }
        enqueueSnackbar(
          "Image uploaded successfully. Once approved the image will be visible in your profile.",
          SUCCESS_TOAST_OPTIONS
        );
        setUploading(false);
        setUserAtomState({
          ...userAtomState,
          imageUploaded: new Date(),
        });
        callback();
      } catch (error) {
        console.error(error);
        setUploading(false);
        enqueueSnackbar("Failed to upload image.", ERROR_TOAST_OPTIONS);
      }
    },
    [setUploading]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: acceptedTypes,
    multiple: false,
    onDrop,
  });

  const classes = useImageUploadStyles();
  const { userId } = useRecoilValue(userAtom);
  const { enqueueSnackbar } = useSnackbar();

  if (uploading)
    return (
      <Grid
        container
        justify="center"
        alignItems="center"
        direction="column"
        className={classes.uploadingWrapper}
      >
        <CircularProgress color="secondary" />
        <Typography
          variant="button"
          display="block"
          style={{ marginTop: "1em" }}
        >
          UPLOADING...
        </Typography>
      </Grid>
    );

  return (
    <div {...getRootProps()} className={classes.dropzone}>
      <input {...getInputProps()} />
      <Box className={classes.dropzoneIcon}>
        <FaCloudUploadAlt />
      </Box>
      <Typography variant="h4" className={classes.dropzoneTitle}>
        Upload Photo
      </Typography>
      <p>Drag and drop file here...</p>
      <p className={classes.textRequirement}>Image size requirements: file size 2MB max, dimensions for avatar 100X100 or for banner 1200X300 min.</p>
    </div>
  );
};

export default ImageUpload;
