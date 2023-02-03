import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Button,
  Dialog,
  Divider,
  Grid,
  Icon,
  TextField,
  Typography,
} from "@material-ui/core";
import DOMPurify from "dompurify";

import { UOneDialogContent, UOneDialogTitle } from "components/UOneDialog";

import { FaqModalProps } from "./type";

import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { faqs } from "./constants";
import { FaRegLifeRing } from "react-icons/fa";
import {
  headings as useHeadingStyles,
  headingIcon as useHeadingIconStyles,
} from "./style";
import { useState } from "react";
import LevelInfoTable from "./levelInfoTable";
import { useForm } from "react-hook-form";
import { RHFInputComponent } from "components";
import { contactSupport } from "services/Api/ContactSupport";
import { useRecoilValue } from "recoil";
import { userAtom } from "state";
import { format } from "date-fns";
const FaqModal: React.FC<FaqModalProps> = ({ open, onClose }) => {
  const headings = useHeadingStyles();
  const headingIcon = useHeadingIconStyles();
  const [buttonStatus, setButtonStatus] = useState(false);
  const { email, fullName, username } = useRecoilValue(userAtom);
  const { control, handleSubmit, reset, setValue } = useForm();
  const [message, setMessage] = useState<string>("");

  const onSubmit = async ({ message }: any) => {
    await contactSupport({
      email: email || username,
      name: fullName,
      message,
      time: format(new Date(), 'hh:mm a'),
      date: format(new Date(), 'MM.dd.yyyy'),
    });
    reset({ message: "" });
    setMessage("Message sent!");
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth={true} maxWidth={"md"}>
      <UOneDialogTitle id="coin-store-create-item" onClose={onClose}>
        Help Center
      </UOneDialogTitle>
      <UOneDialogContent dividers>
        <div>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h3" gutterBottom classes={headings}>
                <Icon
                  color="primary"
                  component={FaRegLifeRing}
                  classes={headingIcon}
                />{" "}
                Welcome to the HeyDay Now Help Center
              </Typography>
              <Typography variant="h4" gutterBottom classes={headings}>
                FAQ
              </Typography>
              {faqs.map((item) => (
                <Accordion key={item.question}>
                  <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                    aria-controls={item.question}
                  >
                    <Typography variant="h5" classes={headings}>
                      {item.question}
                    </Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    {item.answer == "Table" ? (
                      <LevelInfoTable />
                    ) : (
                      <div
                        dangerouslySetInnerHTML={{
                          __html: DOMPurify.sanitize(item.answer),
                        }}
                      />
                    )}
                  </AccordionDetails>
                </Accordion>
              ))}
            </Grid>
            <Grid item xs={12}>
              <Divider />
            </Grid>
            <Grid item xs={12}>
              <Typography variant="h4" gutterBottom classes={headings}>
                Contact
              </Typography>
              <Typography>
                Not finding the information you're looking for? Try direct
                messaging your Admin...
              </Typography>
              {message ? (
                <Typography>
                  {message}.{" "}
                  <Button variant="text" onClick={() => setMessage("")}>
                    Send new message.
                  </Button>
                </Typography>
              ) : (
                <form onSubmit={handleSubmit(onSubmit)}>
                  <Grid container direction="column" spacing={2}>
                    <Grid item>
                      <RHFInputComponent
                        control={control}
                        name="message"
                        label="Message"
                        fullWidth
                        multiline
                        rows={5}
                        variant="standard"
                      />
                    </Grid>
                    <Grid item>
                      <Button
                        type="submit"
                        color="primary"
                        variant="outlined"
                        disabled={buttonStatus}
                      >
                        Submit
                      </Button>
                    </Grid>
                  </Grid>
                </form>
              )}
            </Grid>
          </Grid>
        </div>
      </UOneDialogContent>
    </Dialog>
  );
};

export default FaqModal;
