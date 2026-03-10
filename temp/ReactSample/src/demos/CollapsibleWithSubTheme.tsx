/**
 * TEST H: Collapsible + SubTheme-like wrapper
 * Tests the nesting order: Grid > SubTheme > Collapsible > children
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { createTheme, ThemeProvider } from '@mui/material/styles';

const subTheme = createTheme({
  palette: {
    primary: { main: '#e91e63' },
    background: { default: '#fce4ec' },
  },
});

export function CollapsibleWithSubTheme() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test H: Collapsible + SubTheme</Typography>

      {/* H1: SubTheme wrapper outside Accordion */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
        H1: SubTheme wraps Accordion (like flex.hbs nesting order)
      </Typography>
      <Grid size={{ xs: 12 }}>
        <ThemeProvider theme={subTheme}>
          <Paper sx={{ p: 0 }}>
            <Accordion defaultExpanded>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h5">Themed Collapsible Group</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Stack spacing={2}>
                  <TextField fullWidth label="Themed Field 1" />
                  <TextField fullWidth label="Themed Field 2" />
                </Stack>
              </AccordionDetails>
            </Accordion>
          </Paper>
        </ThemeProvider>
      </Grid>

      {/* H2: Collapsible with customImplementation-like wrapper */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>
        H2: CustomImplementation-like wrapper + Collapsible
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
        In flex.hbs, customImplementation wraps with ComponentProxy. The Accordion sits inside that.
      </Typography>
      <Grid size={{ xs: 12 }}>
        {/* Simulating ComponentProxy wrapper */}
        <Box sx={{ border: '2px dashed #ccc', p: 1, borderRadius: 1 }}>
          <Typography variant="caption" color="text.secondary">ComponentProxy wrapper</Typography>
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h5">Custom + Collapsible</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Stack spacing={2}>
                <TextField fullWidth label="Custom Field 1" />
                <TextField fullWidth label="Custom Field 2" />
              </Stack>
            </AccordionDetails>
          </Accordion>
        </Box>
      </Grid>

      {/* H3: Full stack: SubTheme + Custom + Collapsible */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>
        H3: Full nesting — SubTheme + Custom + Collapsible
      </Typography>
      <Grid size={{ xs: 12 }}>
        <ThemeProvider theme={subTheme}>
          <Box sx={{ border: '2px dashed pink', p: 1, borderRadius: 1 }}>
            <Typography variant="caption" color="text.secondary">SubTheme + ComponentProxy</Typography>
            <Accordion defaultExpanded>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h5">Full Stack Group</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Stack spacing={2}>
                  <TextField fullWidth label="Full Stack Field 1" />
                  <TextField fullWidth label="Full Stack Field 2" />
                </Stack>
              </AccordionDetails>
            </Accordion>
          </Box>
        </ThemeProvider>
      </Grid>
    </Box>
  );
}
