/**
 * TEST D: Collapsible + Icon only (no label)
 * Edge case: icon in summary without text
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import SettingsIcon from '@mui/icons-material/Settings';
import { SampleChildrenVertical } from './SampleChildren';

export function CollapsibleWithIcon() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test D: Collapsible + Icon Only</Typography>

      {/* D1: Icon only in summary */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>D1: Only icon in AccordionSummary</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <SettingsIcon sx={{ marginRight: 1 }} />
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* D2: Icon with tooltip-like behavior */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>D2: Icon + fallback name as text</Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Grid container direction="row" alignItems="center">
              <SettingsIcon sx={{ marginRight: 1 }} />
              <Typography variant="body1" color="text.secondary">Settings</Typography>
            </Grid>
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
