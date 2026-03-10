/**
 * TEST A: Collapsible Only (no card, no label, no icon)
 * Question: What does a bare collapsible look like?
 * Uses: MUI Accordion
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { SampleChildrenVertical } from './SampleChildren';

export function CollapsibleOnly() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test A: Collapsible Only</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Bare minimum: collapsible=true, no card, no label, no icon.
        Should this even be allowed? What does the summary show?
      </Typography>

      {/* Option A1: Accordion with generic summary text */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>A1: Accordion with fallback text "Details"</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>Details</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* Option A2: Accordion with no summary text — just the expand icon */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>A2: Accordion with empty summary</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} />
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* Option A3: Starts collapsed */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>A3: Starts collapsed (not defaultExpanded)</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>Click to expand</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>
    </Box>
  );
}
